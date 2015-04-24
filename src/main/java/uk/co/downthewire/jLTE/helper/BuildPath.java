package uk.co.downthewire.jLTE.helper;

import org.apache.commons.io.FileUtils;
import java.io.File;
import uk.co.downthewire.jLTE.simulator.utils.FieldNames;
import org.apache.commons.configuration.Configuration;

public class BuildPath {
    public static String getFadingPath(Configuration config) {
        return FileUtils.getUserDirectoryPath() + File.separator +
                config.getString(FieldNames.FADING_PATH) + File.separator;
    }

}
