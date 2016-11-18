/*
 * Copyright 2016 Sascha Peilicke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package saschpe.birthdays.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import saschpe.birthdays.R;
import saschpe.birthdays.adapter.base.ArrayAdapter;
import saschpe.birthdays.model.AccountModel;

public final class AccountArrayAdapter extends ArrayAdapter<AccountModel, AccountArrayAdapter.AccountViewHolder> {
    private final LayoutInflater inflater;
    private OnAccountSelectedListener onAccountSelectedListener;

    public interface OnAccountSelectedListener {
        void onAccountSelected(AccountModel accountModel);
    }

    public AccountArrayAdapter(@NonNull Context context, List<AccountModel> objects) {
        super(objects);
        inflater = LayoutInflater.from(context);
    }

    public void setOnAccountSelectedListener(OnAccountSelectedListener onAccountSelectedListener) {
        this.onAccountSelectedListener = onAccountSelectedListener;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AccountViewHolder(inflater.inflate(R.layout.view_account, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final AccountViewHolder holder, int position) {
        final AccountModel item = getItem(position);

        holder.label.setText(item.getLabel() + " (" + item.getAccount().name + ")");
        holder.icon.setImageDrawable(item.getIcon());
        holder.selected.setChecked(item.isSelected());
        holder.selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.toggleSelected();
                if (onAccountSelectedListener != null) {
                    onAccountSelectedListener.onAccountSelected(item);
                }
            }
        });
    }

    static final class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        ImageView icon;
        CheckBox selected;

        AccountViewHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.label);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            selected = (CheckBox) itemView.findViewById(R.id.selected);
        }
    }
}
