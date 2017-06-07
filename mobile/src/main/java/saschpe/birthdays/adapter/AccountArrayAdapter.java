/*
 * Copyright (C) 2016 Sascha Peilicke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import android.widget.TextView;

import java.util.List;

import saschpe.android.utils.adapter.base.ArrayAdapter;
import saschpe.birthdays.R;
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

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AccountViewHolder(inflater.inflate(R.layout.view_account, parent, false));
    }

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
        final TextView label;
        final ImageView icon;
        final CheckBox selected;

        AccountViewHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.label);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            selected = (CheckBox) itemView.findViewById(R.id.selected);
        }
    }
}
