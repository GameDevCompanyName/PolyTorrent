package ru.gamedevcompanyname.polytorrent.tracker;

import java.io.File;
import java.io.InputStream;

public class MetaDataProvider {

    public MetaDataProvider() {

    }

    public MetaData provide(InputStream in) {
        return new MetaData();
    }

}
