package ru.kmorozov.onedrive.client;

import com.google.api.client.util.Throwables;
import ru.kmorozov.onedrive.client.facets.FolderFacet;
import ru.kmorozov.onedrive.client.resources.Item;
import ru.kmorozov.onedrive.client.resources.ItemReference;
import ru.kmorozov.onedrive.client.serialization.JsonDateSerializer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

public interface OneDriveItem {
    String getId();

    boolean isDirectory();

    String getName();

    String getFullName();

    long getCrc32();

    long getSize();

    Date getCreatedDateTime();

    Date getLastModifiedDateTime();

    OneDriveItem getParent();

    FolderFacet getFolder();

    class FACTORY {

        public static OneDriveItem create(final OneDriveItem parent, final String name, final boolean isDirectory) {

            return new OneDriveItem() {
                public String getId() {
                    return null;
                }

                public boolean isDirectory() {
                    return isDirectory;
                }

                public String getName() {
                    return name;
                }

                public String getFullName() {
                    return parent.getFullName() + name + (isDirectory ? "/" : "");
                }

                @Override
                public long getCrc32() {
                    return 0L;
                }

                @Override
                public long getSize() {
                    return 0L;
                }

                @Override
                public Date getCreatedDateTime() {
                    return null;
                }

                @Override
                public Date getLastModifiedDateTime() {
                    return null;
                }

                @Override
                public OneDriveItem getParent() {
                    return parent;
                }

                @Override
                public FolderFacet getFolder() {
                    return null;
                }
            };
        }

        public static OneDriveItem create(final Item item) {
            return new OneDriveItem() {

                private final OneDriveItem parent = create(item.getParentReference());

                @Override
                public String getId() {
                    return item.getId();
                }

                @Override
                public boolean isDirectory() {
                    return null != item.getFolder();
                }

                @Override
                public String getName() {
                    return item.getName();
                }

                @Override
                public String getFullName() {
                    return parent.getFullName() + item.getName() + (isDirectory() ? "/" : "");
                }

                @Override
                public long getCrc32() {
                    return item.getFile().getHashes().getCrc32();
                }

                @Override
                public long getSize() {
                    return item.getSize();
                }

                @Override
                public Date getCreatedDateTime() {
                    try {
                        return JsonDateSerializer.INSTANCE.deserialize(item.getFileSystemInfo().getCreatedDateTime());
                    } catch (ParseException e) {
                        e.printStackTrace();

                        return new Date();
                    }
                }

                @Override
                public Date getLastModifiedDateTime() {
                    try {
                        return JsonDateSerializer.INSTANCE.deserialize(item.getFileSystemInfo().getLastModifiedDateTime());
                    } catch (ParseException e) {
                        e.printStackTrace();

                        return new Date();
                    }
                }

                @Override
                public OneDriveItem getParent() {
                    return parent;
                }

                @Override
                public FolderFacet getFolder() {
                    return item.getFolder();
                }
            };
        }

        public static OneDriveItem create(final ItemReference parent) {
            return new OneDriveItem() {
                @Override
                public String getId() {
                    return null == parent ? null : parent.getId();
                }

                @Override
                public boolean isDirectory() {
                    return true;
                }

                @Override
                public String getName() {
                    return null;
                }

                public String getFullName() {

                    if (null == parent.getPath()) {
                        return null;
                    }

                    final int index = parent.getPath().indexOf(':');

                    return URLDecoder.decode(0 < index ? parent.getPath().substring(index + 1) : parent.getPath(), StandardCharsets.UTF_8) + '/';
                }

                @Override
                public long getCrc32() {
                    return 0L;
                }

                @Override
                public long getSize() {
                    return 0L;
                }

                @Override
                public Date getCreatedDateTime() {
                    return null;
                }

                @Override
                public Date getLastModifiedDateTime() {
                    return null;
                }

                @Override
                public OneDriveItem getParent() {
                    return null;
                }

                @Override
                public FolderFacet getFolder() {
                    return null;
                }
            };
        }
    }
}
