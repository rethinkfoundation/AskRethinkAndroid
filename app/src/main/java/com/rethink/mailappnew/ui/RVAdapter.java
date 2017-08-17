package com.rethink.mailappnew.ui;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rethink.ama.R;
import com.rethink.mailappnew.controller.db.model.Contact;

import java.util.List;

/**
 * Created by Shibin.co on 09/07/17.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>{


    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView personName;
        TextView personNumber;
        TextView personEmail;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            personName = (TextView)itemView.findViewById(R.id.person_name);
            personNumber = (TextView)itemView.findViewById(R.id.person_number);
            personEmail = (TextView)itemView.findViewById(R.id.person_email);
        }

    }
    List<Contact> mContact;
    Context mContext;

    RVAdapter(Context c,List<Contact> contacts){
        this.mContact = contacts;
        this.mContext = c;

    }

    @Override
    public int getItemCount() {
        Log.e("TAG","size = "+mContact.size());
        return mContact.size();
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_item, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }


    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        Log.e("TAG","onBindViewHolder "+i);
        Contact item = mContact.get(i);
        personViewHolder.personName.setText(item.getName());
        personViewHolder.personNumber.setText(item.getNumber());
        personViewHolder.personEmail.setText(item.getEmail());
    }
}
