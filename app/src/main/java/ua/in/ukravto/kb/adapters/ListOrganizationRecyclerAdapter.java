package ua.in.ukravto.kb.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import ua.in.ukravto.kb.databinding.ItemOrginizationBinding;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;

public class ListOrganizationRecyclerAdapter extends RecyclerView.Adapter<ListOrganizationRecyclerAdapter.ViewHolder> implements Filterable {

    private List<EmployeeOrganizationModel> data;
    private List<EmployeeOrganizationModel> dataFiltered;

    public ListOrganizationRecyclerAdapter() {
        data = new ArrayList<>();
        dataFiltered = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemOrginizationBinding mBinding = ItemOrginizationBinding.inflate(layoutInflater, parent, false);

        return new ViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeeOrganizationModel item = getItemForPosition(position);
        holder.mBinding.setOrganization(item);
        holder.mBinding.executePendingBindings();

    }

    @Override
    public int getItemCount() {
        return dataFiltered.size();
    }

    public EmployeeOrganizationModel getItemForPosition(int position){
        return dataFiltered.get(position);
    }

    public void setData(List<EmployeeOrganizationModel> data) {
        this.data = data;
        this.dataFiltered = data;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    dataFiltered = data;
                } else {
                    List<EmployeeOrganizationModel> filteredList = new ArrayList<>();
                    for (EmployeeOrganizationModel row : data) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    dataFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = dataFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                dataFiltered = (ArrayList<EmployeeOrganizationModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

        public final ItemOrginizationBinding mBinding;

        public ViewHolder(ItemOrginizationBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            mBinding.checkOrganization.setOnCheckedChangeListener(this);
            mBinding.getRoot().setOnClickListener(this);
            mBinding.textNameOrganization.setOnClickListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            mBinding.getOrganization().setIsChecked(b);
        }

        @Override
        public void onClick(View view) {
            mBinding.getOrganization().setIsChecked(!mBinding.getOrganization().getIsChecked());
            mBinding.checkOrganization.setChecked(mBinding.getOrganization().getIsChecked());
        }
    }
}
