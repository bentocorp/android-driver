/**
 * @author Kokusho Torres
 * 27/10/2015
 */
package com.bentonow.drive.widget.wrapper;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.drive.R;


public class ItemOrderWrapper extends RecyclerView.ViewHolder {

    public View view = null;

    private ImageView img_order_status = null;
    private TextView txt_street = null;
    private TextView txt_city = null;
    private TextView txt_user_order = null;


    public ItemOrderWrapper(View base) {
        super(base);
        this.view = base;
    }

    public ImageView getImgOrderStatus() {
        if (img_order_status == null)
            img_order_status = (ImageView) view.findViewById(R.id.img_order_status);
        return img_order_status;
    }

    public TextView getTxtStreet() {
        if (txt_street == null)
            txt_street = (TextView) view.findViewById(R.id.txt_street);
        return txt_street;
    }


    public TextView getTxtCity() {
        if (txt_city == null)
            txt_city = (TextView) view.findViewById(R.id.txt_city);
        return txt_city;
    }

    public TextView getTxtUserName() {
        if (txt_user_order == null)
            txt_user_order = (TextView) view.findViewById(R.id.txt_user_order);
        return txt_user_order;
    }

}
