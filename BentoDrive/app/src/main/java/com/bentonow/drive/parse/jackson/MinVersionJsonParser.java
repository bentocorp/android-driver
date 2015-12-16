package com.bentonow.drive.parse.jackson;

import com.bentonow.drive.model.VersionModel;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Jose Torres on 18/05/15.
 */
public class MinVersionJsonParser extends MainParser {

    public static final String TAG = "MinVersionJsonParser";

    public static VersionModel parseMinVersion(String json) throws Exception {

        VersionModel mVersion = new VersionModel();

        startParsed();

        jsonFactory = new ObjectMapper().getFactory();
        jp = jsonFactory.createParser(json);
        jp.nextToken();
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            nameField = jp.getCurrentName();
            jp.nextToken();
            if ("code".equals(nameField)) {
                mVersion.setiCode(jp.getIntValue());
            } else if ("msg".equals(nameField)) {
                mVersion.setsMessage(jp.getText());
            } else if ("ret".equals(nameField)) {
                while (jp.nextToken() != JsonToken.END_OBJECT) {
                    nameField = jp.getCurrentName();
                    jp.nextToken();
                    if (TAG_MIN_VERSION.equals(nameField)) {
                        mVersion.setMin_version(jp.getText());
                    } else if (TAG_MIN_VERSION_URL.equals(nameField)) {
                        mVersion.setMin_version_url(jp.getText());
                    } else {
                        tagNotFound();
                    }
                }
            } else {
                tagNotFound();
            }
        }

        stopParsed();

        return mVersion;
    }

}
