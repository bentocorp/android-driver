/**
 *
 */
package com.bentonow.drive.controller.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bentonow.drive.R;
import com.bentonow.drive.listener.RecyclerListListener;
import com.bentonow.drive.model.OrderItemModel;
import com.bentonow.drive.util.DebugUtils;
import com.bentonow.drive.widget.wrapper.ItemOrderWrapper;

import java.util.ArrayList;

/**
 * @author José Torres Fuentes
 */

public class OrderListAdapter extends RecyclerView.Adapter<ItemOrderWrapper> {

    public static final String TAG = "OrderListAdapter";

    private Activity mActivity;
    private RecyclerListListener mClickListener;
    public ArrayList<OrderItemModel> aListOrder = new ArrayList<>();

    /**
     * @param context
     */
    public OrderListAdapter(Activity context, RecyclerListListener mClickListener) {
        this.mActivity = context;
        this.mClickListener = mClickListener;
    }

    @Override
    public ItemOrderWrapper onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_order_bento, parent, false);
        ItemOrderWrapper vh = new ItemOrderWrapper(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ItemOrderWrapper viewHolder, final int position) {
        final OrderItemModel mOrder = aListOrder.get(position);

        viewHolder.getTxtStreet().setText(mOrder.getAddress().getStreet());
        viewHolder.getTxtCity().setText(mOrder.getAddress().getCity());
        viewHolder.getTxtUserName().setText(mOrder.getName());

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null)
                    mClickListener.OnItemClickListener(position);
            }
        });

        DebugUtils.logDebug(TAG, mOrder.getStatus());

        switch (mOrder.getStatus()) {
            case "PENDING":
                viewHolder.getImgOrderStatus().setImageDrawable(mActivity.getResources().getDrawable(R.drawable.circle_yellow));
                break;
            case "ACCEPTED":
                viewHolder.getImgOrderStatus().setImageDrawable(mActivity.getResources().getDrawable(R.drawable.circle_blue));
                break;
            case "REJECTED":
                viewHolder.getImgOrderStatus().setImageDrawable(mActivity.getResources().getDrawable(R.drawable.circle_red));
                break;
            default:
                viewHolder.getImgOrderStatus().setImageDrawable(mActivity.getResources().getDrawable(R.drawable.background_transparent));
                break;
        }

    }


    @Override
    public int getItemCount() {
        return aListOrder.size();
    }


}
