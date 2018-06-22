package ua.in.ukravto.kb.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

import ua.in.ukravto.kb.databinding.ItemOrginizationBinding;
import ua.in.ukravto.kb.repository.database.model.EmployeeOrganizationModel;

public class ListOrganizationRecyclerAdapter extends RecyclerView.Adapter<ListOrganizationRecyclerAdapter.ViewHolder> {

    private List<EmployeeOrganizationModel> data;

    public ListOrganizationRecyclerAdapter() {
        data = new ArrayList<>();
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
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public EmployeeOrganizationModel getItemForPosition(int position){
        return data.get(position);
    }

    public void setData(List<EmployeeOrganizationModel> data) {
        this.data = data;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        private final ItemOrginizationBinding mBinding;

        public ViewHolder(ItemOrginizationBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            mBinding.checkOrganization.setOnCheckedChangeListener(this);
        }

        public void bind(EmployeeOrganizationModel item) {
            mBinding.setOrganization(item);
            mBinding.executePendingBindings();
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            mBinding.getOrganization().setIsChecked(b);
        }
    }

}
