/**
 * @author Ahmed-Elshahat
 */
package com.appium.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<HashMap<String, String>> getJsonDataToMap(String jsonFilePath) throws IOException {
        String completePath = System.getProperty("user.dir") + "/src/test/resources/testdata/" + jsonFilePath;
        return mapper.readValue(new File(completePath), new TypeReference<List<HashMap<String, String>>>() {
        });
    }
}
