package com.bentonow.drive.parse.jackson;

import com.bentonow.drive.model.DishItemModel;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.util.DebugUtils;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

/**
 * Created by Jose Torres on 18/05/15.
 */
public class BentoOrderJsonParser extends MainParser {

    public static final String TAG = "BentoOrderJsonParser";

    public static ArrayList<OrderItemModel> parseBentoListOrder(String json) {

        final String twoPoints = "abcd";
        json = json.replace(":\\n", twoPoints);
        json = json.replace(": \\n", twoPoints);

        ArrayList<OrderItemModel> mArrayBentoOrder = new ArrayList<>();

        startParsed();
        try {
            jsonFactory = new ObjectMapper().getFactory();
            jp = jsonFactory.createParser(json);
            jp.nextToken();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                nameField = jp.getCurrentName();
                jp.nextToken();
                if ("code".equals(nameField)) {
                    int iCode = jp.getIntValue();
                    if (iCode != 0)
                        break;
                } else if ("ret".equals(nameField)) {
                    while (jp.nextToken() != JsonToken.END_ARRAY) {
                        OrderItemModel mOrder = new OrderItemModel();
                        String sNote = "";
                        mOrder.setOrderType("ASSIGN");
                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                            nameField = jp.getCurrentName();
                            jp.nextToken();
                            if (TAG_ID.equals(nameField)) {
                                mOrder.setOrderId(jp.getText());
                            } else if (TAG_NAME.equals(nameField)) {
                                mOrder.setName(jp.getText());
                            } else if (TAG_PHONE.equals(nameField)) {
                                mOrder.setPhone(jp.getText());
                            } else if (TAG_ADDRESS.equals(nameField)) {
                                while (jp.nextToken() != JsonToken.END_OBJECT) {
                                    nameField = jp.getCurrentName();
                                    jp.nextToken();
                                    if (TAG_STREET.equals(nameField)) {
                                        mOrder.getAddress().setStreet(jp.getText());
                                    } else if (TAG_CITY.equals(nameField)) {
                                        mOrder.getAddress().setCity(jp.getText());
                                    } else if (TAG_RESIDENCE.equals(nameField)) {
                                        mOrder.getAddress().setResidence(jp.getText());
                                    } else if (TAG_REGION.equals(nameField)) {
                                        mOrder.getAddress().setRegion(jp.getText());
                                    } else if (TAG_ZIPCODE.equals(nameField)) {
                                        mOrder.getAddress().setZipCode(jp.getText());
                                    } else if (TAG_COUNTRY.equals(nameField)) {
                                        mOrder.getAddress().setCountry(jp.getText());
                                    } else if (TAG_LAT.equals(nameField)) {
                                        mOrder.getAddress().setLat(jp.getDoubleValue());
                                    } else if (TAG_LNG.equals(nameField)) {
                                        mOrder.getAddress().setLng(jp.getDoubleValue());
                                    } else {
                                        tagNotFound();
                                    }
                                }
                            } else if (TAG_ITEM.equals(nameField)) {
                                if (mOrder.getOrderId().startsWith("g")) {
                                    mOrder.setItem(jp.getText());
                                } else {
                                    int nBento = 0;
                                    while (jp.nextToken() != JsonToken.END_ARRAY) {
                                        nBento++;
                                        mOrder.setItem(mOrder.getItem() + "? BENTO " + nBento + " of xyz \n ====\n");
                                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                                            nameField = jp.getCurrentName();
                                            jp.nextToken();
                                            if (TAG_ITEMS.equals(nameField)) {
                                                while (jp.nextToken() != JsonToken.END_ARRAY) {
                                                    DishItemModel mDishModel = new DishItemModel();
                                                    while (jp.nextToken() != JsonToken.END_OBJECT) {
                                                        nameField = jp.getCurrentName();
                                                        jp.nextToken();
                                                        if (TAG_ID.equals(nameField)) {
                                                            mDishModel.setId(jp.getText());
                                                        } else if (TAG_NAME.equals(nameField)) {
                                                            mDishModel.setName(jp.getText());
                                                        } else if (TAG_TYPE.equals(nameField)) {
                                                            mDishModel.setType(jp.getText());
                                                        } else if (TAG_LABEL.equals(nameField)) {
                                                            mDishModel.setLabel(jp.getText());
                                                        } else {
                                                            tagNotFound();
                                                        }
                                                    }
                                                    mOrder.setItem(mOrder.getItem() + mDishModel.getLabel() + " - " + mDishModel.getName() + "\n");
                                                }
                                            } else {
                                                tagNotFound();
                                            }
                                        }
                                        mOrder.setItem(mOrder.getItem() + " ====\n\n");
                                    }
                                    mOrder.setItem(mOrder.getItem() + ">> Is everything accurate? \n\n>> Don't forget:\n + mochi!\n + to ask which type of soy sauce\n + to offer utensils \n\n");
                                    mOrder.setItem(mOrder.getItem().replace("xyz", String.valueOf(nBento)));
                                }
                            } else if (TAG_KEY.equals(nameField)) {
                                mOrder.setKey(jp.getIntValue());
                            } else if (TAG_ORDERSTRING.equals(nameField)) {
                                String sItem = jp.getText();
                                if (sItem != null && !sItem.isEmpty())
                                    mOrder.setItem(sItem.replace(twoPoints, ": \\n"));
                            } else if (TAG_NOTES.equals(nameField)) {
                                sNote = jp.getText();
                            } else if (TAG_DRIVERID.equals(nameField)) {
                                mOrder.setDriverId(jp.getText());
                            } else if (TAG_STATUS.equals(nameField)) {
                                mOrder.setStatus(jp.getText());
                            } else {
                                tagNotFound();
                            }
                        }
                        String sOrderText = "";
                        if (!sNote.isEmpty())
                            sOrderText += "NOTES:\n ====\n" + sNote + "\n ====\n\n";
                        sOrderText += mOrder.getItem().replace("\\n", "\n");

                        mOrder.setItem(sOrderText);
                        if (!mOrder.getStatus().equals("COMPLETE"))
                            mArrayBentoOrder.add(mOrder);
                    }

                } else {
                    tagNotFound();
                }
            }
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);

        }

        stopParsed();

        DebugUtils.logDebug(TAG, "Num Of Orders: " + mArrayBentoOrder.size());

        return mArrayBentoOrder;
    }

    public static OrderItemModel parseBentoOrderItem(String json) throws Exception {

        final String twoPoints = "abcd";
        json = json.replace(":\\n", twoPoints);
        json = json.replace(": \\n", twoPoints);

        OrderItemModel mOrder = new OrderItemModel();
        String sNote = "";

        startParsed();

        jsonFactory = new ObjectMapper().getFactory();
        jp = jsonFactory.createParser(json);
        jp.nextToken();
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            nameField = jp.getCurrentName();
            jp.nextToken();
            if ("body".equals(nameField)) {
                while (jp.nextToken() != JsonToken.END_OBJECT) {
                    nameField = jp.getCurrentName();
                    jp.nextToken();
                    if (TAG_TYPE.equals(nameField)) {
                        mOrder.setOrderType(jp.getText());
                    } else if (TAG_ORDER.equals(nameField)) {
                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                            nameField = jp.getCurrentName();
                            jp.nextToken();
                            if (TAG_ID.equals(nameField)) {
                                mOrder.setOrderId(jp.getText());
                            } else if (TAG_NAME.equals(nameField)) {
                                mOrder.setName(jp.getText());
                            } else if (TAG_PHONE.equals(nameField)) {
                                mOrder.setPhone(jp.getText());
                            } else if (TAG_ADDRESS.equals(nameField)) {
                                while (jp.nextToken() != JsonToken.END_OBJECT) {
                                    nameField = jp.getCurrentName();
                                    jp.nextToken();
                                    if (TAG_STREET.equals(nameField)) {
                                        mOrder.getAddress().setStreet(jp.getText());
                                    } else if (TAG_CITY.equals(nameField)) {
                                        mOrder.getAddress().setCity(jp.getText());
                                    } else if (TAG_RESIDENCE.equals(nameField)) {
                                        mOrder.getAddress().setResidence(jp.getText());
                                    } else if (TAG_REGION.equals(nameField)) {
                                        mOrder.getAddress().setRegion(jp.getText());
                                    } else if (TAG_ZIPCODE.equals(nameField)) {
                                        mOrder.getAddress().setZipCode(jp.getText());
                                    } else if (TAG_COUNTRY.equals(nameField)) {
                                        mOrder.getAddress().setCountry(jp.getText());
                                    } else if (TAG_LAT.equals(nameField)) {
                                        mOrder.getAddress().setLat(jp.getDoubleValue());
                                    } else if (TAG_LNG.equals(nameField)) {
                                        mOrder.getAddress().setLng(jp.getDoubleValue());
                                    } else {
                                        tagNotFound();
                                    }
                                }
                            } else if (TAG_ITEM.equals(nameField)) {
                                if (mOrder.getOrderId().startsWith("g")) {
                                    mOrder.setItem(jp.getText());
                                } else {
                                    int nBento = 0;
                                    while (jp.nextToken() != JsonToken.END_ARRAY) {
                                        nBento++;
                                        mOrder.setItem(mOrder.getItem() + "? BENTO " + nBento + " of xyz \n ====\n");
                                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                                            nameField = jp.getCurrentName();
                                            jp.nextToken();
                                            if (TAG_ITEMS.equals(nameField)) {
                                                while (jp.nextToken() != JsonToken.END_ARRAY) {
                                                    DishItemModel mDishModel = new DishItemModel();
                                                    while (jp.nextToken() != JsonToken.END_OBJECT) {
                                                        nameField = jp.getCurrentName();
                                                        jp.nextToken();
                                                        if (TAG_ID.equals(nameField)) {
                                                            mDishModel.setId(jp.getText());
                                                        } else if (TAG_NAME.equals(nameField)) {
                                                            mDishModel.setName(jp.getText());
                                                        } else if (TAG_TYPE.equals(nameField)) {
                                                            mDishModel.setType(jp.getText());
                                                        } else if (TAG_LABEL.equals(nameField)) {
                                                            mDishModel.setLabel(jp.getText());
                                                        } else {
                                                            tagNotFound();
                                                        }
                                                    }
                                                    mOrder.setItem(mOrder.getItem() + mDishModel.getLabel() + " - " + mDishModel.getName() + "\n");
                                                }
                                            } else {
                                                tagNotFound();
                                            }
                                        }
                                        mOrder.setItem(mOrder.getItem() + " ====\n\n");
                                    }
                                    mOrder.setItem(mOrder.getItem() + ">> Is everything accurate? \n\n>> Don't forget:\n + mochi!\n + to ask which type of soy sauce\n + to offer utensils \n\n");
                                    mOrder.setItem(mOrder.getItem().replace("xyz", String.valueOf(nBento)));
                                }
                            } else if (TAG_KEY.equals(nameField)) {
                                mOrder.setKey(jp.getIntValue());
                            } else if (TAG_ORDERSTRING.equals(nameField)) {
                                String sItem = jp.getText();
                                if (sItem != null && !sItem.isEmpty())
                                    mOrder.setItem(sItem.replace(twoPoints, ": \\n"));
                            } else if (TAG_NOTES.equals(nameField)) {
                                sNote = jp.getText();
                            } else if (TAG_DRIVERID.equals(nameField)) {
                                mOrder.setDriverId(jp.getText());
                            } else if (TAG_STATUS.equals(nameField)) {
                                mOrder.setStatus(jp.getText());
                            } else {
                                tagNotFound();
                            }
                        }
                    } else if (TAG_AFTER.equals(nameField)) {
                        mOrder.setAfter(jp.getText());
                    } else {
                        tagNotFound();
                    }
                }
                String sOrderText = "";
                if (!sNote.isEmpty())
                    sOrderText += "NOTES:\n ====\n" + sNote + "\n ====\n\n";
                sOrderText += mOrder.getItem().replace("\\n", "\n");
                mOrder.setItem(sOrderText);

            } else {
                tagNotFound();
            }
        }

        stopParsed();

        return mOrder;
    }

}
