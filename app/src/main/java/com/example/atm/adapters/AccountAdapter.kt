package com.example.atm.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.atm.R
import com.example.sql.DBFunctions

class AccountsAdapter(
    private val accounts: List<DBFunctions.Account>?,
    private val onItemClick: (DBFunctions.Account) -> Unit
) : RecyclerView.Adapter<AccountsAdapter.AccountViewHolder>() {

    class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.accountLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.account_item_layout, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account: DBFunctions.Account = accounts?.get(position) ?: DBFunctions.Account(
            account_id = 0,
            user_id = 0,
            account_type_id = 0,
            balance = 0.0,
            date_opened = ""
        )
        val label = "Account #${account.account_id} (Balance: \$${String.format("%.2f", account.balance)})"
        holder.textView.text = label

        holder.itemView.setOnClickListener {
            onItemClick(account)
        }
    }

    override fun getItemCount(): Int = accounts?.size ?: 0
}