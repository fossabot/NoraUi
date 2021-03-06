/**
 * NoraUi is licensed under the license GNU AFFERO GENERAL PUBLIC LICENSE
 * 
 * @author Nicolas HALLOUIN
 * @author Stéphane GRILLON
 */
package com.github.noraui.application.model;

import com.github.noraui.model.Model;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @deprecated since NoraUi 3.3.0, because DemoModel renamed to CommonModel.
 *             {@link com.github.noraui.application.model.CommonModel} instead.
 */
@Deprecated
public abstract class DemoModel implements Model {

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize() {
        final GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        builder.disableHtmlEscaping();
        final Gson gson = builder.create();
        return gson.toJson(this);
    }

}
