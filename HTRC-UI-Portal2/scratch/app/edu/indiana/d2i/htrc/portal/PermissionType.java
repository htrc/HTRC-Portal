package edu.indiana.d2i.htrc.portal;

public enum PermissionType {

    ALLOW	(1),
    DENY	(2);

    private final int _id;

    private PermissionType(int id) {
        _id = id;
    }

    public String getPermissionType() {
        return Integer.toString(_id);
    }

    @Override
    public String toString() {
        return getPermissionType();
    }
}

