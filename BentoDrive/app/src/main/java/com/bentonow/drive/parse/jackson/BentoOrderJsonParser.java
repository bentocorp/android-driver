package com.bentonow.drive.parse.jackson;

import com.bentonow.drive.model.DishItemModel;
import com.bentonow.drive.model.MenuItemModel;
import com.bentonow.drive.model.OrderItemModel;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Jose Torres on 18/05/15.
 */
public class BentoOrderJsonParser extends MainParser {

    public static OrderItemModel parseBentoListOrder(String json) throws Exception {

        ArrayList<OrderItemModel> mArrayBentoOrder = new ArrayList<>();

        startParsed();

        jsonFactory = new ObjectMapper().getFactory();
        jp = jsonFactory.createParser(json);
        jp.nextToken();
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            nameField = jp.getCurrentName();
            jp.nextToken();
            if ("ret".equals(nameField)) {
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    OrderItemModel mOrder = new OrderItemModel();
                    mOrder.setOrderType(0);
                    while (jp.nextToken() != JsonToken.END_OBJECT) {
                        nameField = jp.getCurrentName();
                        jp.nextToken();
                        if (TAG_ID.equals(nameField)) {
                            mOrder.setId(jp.getText());
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
                            while (jp.nextToken() != JsonToken.END_ARRAY) {
                                MenuItemModel mMenuModel = new MenuItemModel();
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
                                                    mDishModel.setId(jp.getIntValue());
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
                                            mMenuModel.getItems().add(mDishModel);
                                        }
                                    } else {
                                        tagNotFound();
                                    }
                                }
                                mOrder.getItems().add(mMenuModel);
                            }
                        } else if (TAG_KEY.equals(nameField)) {
                            mOrder.setKey(jp.getIntValue());
                        } else if (TAG_ORDERSTRING.equals(nameField)) {
                            mOrder.setOrderString(jp.getText());
                        } else if (TAG_DRIVERID.equals(nameField)) {
                            mOrder.setDriverId(jp.getIntValue());
                        } else if (TAG_STATUS.equals(nameField)) {
                            mOrder.setStatus(jp.getText());
                        } else {
                            tagNotFound();
                        }
                    }
                    mArrayBentoOrder.add(mOrder);
                }

            } else {
                tagNotFound();
            }
        }

        stopParsed();

        return mArrayBentoOrder.get(0);
    }

    public static OrderItemModel parseBentoTask(String json) throws Exception {

        ArrayList<OrderItemModel> mArrayBentoOrder = new ArrayList<>();

        startParsed();

        jsonFactory = new ObjectMapper().getFactory();
        jp = jsonFactory.createParser(json);
        jp.nextToken();
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            nameField = jp.getCurrentName();
            jp.nextToken();
            if ("ret".equals(nameField)) {
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    OrderItemModel mOrder = new OrderItemModel();
                    mOrder.setOrderType(1);
                    while (jp.nextToken() != JsonToken.END_OBJECT) {
                        nameField = jp.getCurrentName();
                        jp.nextToken();
                        if (TAG_ID.equals(nameField)) {
                            mOrder.setId(jp.getText());
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
                            mOrder.setTask(jp.getText());
                        } else if (TAG_KEY.equals(nameField)) {
                            mOrder.setKey(jp.getIntValue());
                        } else if (TAG_ORDERSTRING.equals(nameField)) {
                            mOrder.setOrderString(jp.getText());
                        } else if (TAG_DRIVERID.equals(nameField)) {
                            mOrder.setDriverId(jp.getIntValue());
                        } else if (TAG_STATUS.equals(nameField)) {
                            mOrder.setStatus(jp.getText());
                        } else {
                            tagNotFound();
                        }
                    }
                    mArrayBentoOrder.add(mOrder);
                }
            } else {
                tagNotFound();
            }
        }

        stopParsed();

        return mArrayBentoOrder.get(0);
    }

}
