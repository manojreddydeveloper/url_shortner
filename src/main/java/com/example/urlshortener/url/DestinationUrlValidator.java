package com.example.urlshortener.url;

import java.net.URI;
import java.util.regex.Pattern;

/** Validates and preserves approved redirect destinations without contacting them. */
public final class DestinationUrlValidator {

    public static final int MAX_LENGTH = 4_096;
    private static final Pattern HOST_LABEL = Pattern.compile("[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?");
    private static final Pattern IPV4 = Pattern.compile("[0-9.]+");
    private static final Pattern IPV6 = Pattern.compile("[0-9A-Fa-f:.]+");

    public String validate(String value) {
        if (value == null || value.isEmpty() || value.length() > MAX_LENGTH) {
            throw invalid();
        }
        rejectUnsafeCharacters(value);

        URI uri;
        try {
            uri = new URI(value);
        } catch (IllegalArgumentException | java.net.URISyntaxException exception) {
            throw invalid();
        }

        if (!isHttp(uri.getScheme()) || uri.getRawAuthority() == null
                || uri.getUserInfo() != null || uri.getHost() == null
                || !validPort(uri.getPort()) || !validHost(uri.getHost())) {
            throw invalid();
        }
        return value;
    }

    private static void rejectUnsafeCharacters(String value) {
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character > 0x7f || Character.isWhitespace(character)
                    || character < 0x20 || character == 0x7f) {
                throw invalid();
            }
            if (character == '%') {
                if (i + 2 >= value.length() || hex(value.charAt(i + 1)) < 0
                        || hex(value.charAt(i + 2)) < 0) {
                    throw invalid();
                }
                int decoded = hex(value.charAt(i + 1)) * 16 + hex(value.charAt(i + 2));
                if (decoded < 0x20 || decoded == 0x7f) {
                    throw invalid();
                }
                i += 2;
            }
        }
    }

    private static boolean validHost(String host) {
        if (host.equalsIgnoreCase("localhost")) {
            return true;
        }
        if (host.startsWith("[") && host.endsWith("]")) {
            return host.length() > 2 && IPV6.matcher(host.substring(1, host.length() - 1)).matches();
        }
        if (IPV4.matcher(host).matches()) {
            String[] octets = host.split("\\.", -1);
            if (octets.length != 4) {
                return false;
            }
            for (String octet : octets) {
                try {
                    if (octet.isEmpty() || Integer.parseInt(octet) > 255) {
                        return false;
                    }
                } catch (NumberFormatException exception) {
                    return false;
                }
            }
            return true;
        }
        String[] labels = host.split("\\.", -1);
        if (labels.length == 0) {
            return false;
        }
        for (String label : labels) {
            if (label.isEmpty() || label.length() > 63 || !HOST_LABEL.matcher(label).matches()) {
                return false;
            }
        }
        return host.length() <= 253;
    }

    private static boolean validPort(int port) {
        return port == -1 || (port >= 1 && port <= 65535);
    }

    private static boolean isHttp(String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    private static int hex(char value) {
        if (value >= '0' && value <= '9') return value - '0';
        if (value >= 'a' && value <= 'f') return value - 'a' + 10;
        if (value >= 'A' && value <= 'F') return value - 'A' + 10;
        return -1;
    }

    private static IllegalArgumentException invalid() {
        return new IllegalArgumentException("Destination URL is invalid");
    }
}
