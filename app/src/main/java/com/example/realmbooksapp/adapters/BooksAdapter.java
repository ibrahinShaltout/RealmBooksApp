package com.example.realmbooksapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.realmbooksapp.R;
import com.example.realmbooksapp.app.Prefs;
import com.example.realmbooksapp.model.Book;
import com.example.realmbooksapp.realm.RealmController;

import io.realm.Realm;
import io.realm.RealmResults;

public class BooksAdapter extends RealmRecyclerViewAdapter<Book> {

    final Context context;
    private Realm realm;
    private LayoutInflater inflater;

    public BooksAdapter(Context context) {

        this.context = context;
    }

    // create new views (invoked by the layout manager)
    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate a new card view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_books, parent, false);
        return new CardViewHolder(view);
    }

    // replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        realm = RealmController.getInstance().getRealm();

        // get the article
        final Book book = getItem(position);
        // cast the generic view holder to our specific one
        final CardViewHolder holder = (CardViewHolder) viewHolder;

        // set the title and the snippet
        holder.textTitle.setText(book.getTitle());
        holder.textAuthor.setText(book.getAuthor());
        holder.textDescription.setText(book.getDescription());

        // load the background image
        if (book.getImageUrl() != null) {
            Glide.with(context)
                    .asBitmap()
                    .load(book.getImageUrl().replace("https", "http"))
                    .fitCenter()
                    .into(holder.imageBackground);
        }

        //remove single match from realm
        holder.card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                RealmResults<Book> results = realm.where(Book.class).findAll();

                // Get the book title to show it in toast message
                Book b = results.get(position);
                String title = b.getTitle();

                // All changes to data must happen in a transaction
                realm.beginTransaction();

                // remove single match
                results.remove(position);
                realm.commitTransaction();

                if (results.size() == 0) {
                    Prefs.with(context).setPreLoad(false);
                }

                notifyDataSetChanged();

                Toast.makeText(context, title + " is removed from Realm", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        //update single match from realm
        holder.card.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View content = inflater.inflate(R.layout.edit_item, null);
                final EditText editTitle = (EditText) content.findViewById(R.id.title);
                final EditText editAuthor = (EditText) content.findViewById(R.id.author);
                final EditText editThumbnail = (EditText) content.findViewById(R.id.thumbnail);

                editTitle.setText(book.getTitle());
                editAuthor.setText(book.getAuthor());
                editThumbnail.setText(book.getImageUrl());

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(content)
                        .setTitle("Edit Book")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                RealmResults<Book> results = realm.where(Book.class).findAll();

                                realm.beginTransaction();
                                results.get(position).setAuthor(editAuthor.getText().toString());
                                results.get(position).setTitle(editTitle.getText().toString());
                                results.get(position).setImageUrl(editThumbnail.getText().toString());

                                realm.commitTransaction();

                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    // return the size of your data set (invoked by the layout manager)
    public int getItemCount() {

        if (getRealmAdapter() != null) {
            return getRealmAdapter().getCount();
        }
        return 0;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        public CardView card;
        public TextView textTitle;
        public TextView textAuthor;
        public TextView textDescription;
        public ImageView imageBackground;

        public CardViewHolder(View itemView) {
            // standard view holder pattern with Butterknife view injection
            super(itemView);

            card = (CardView) itemView.findViewById(R.id.card_books);
            textTitle = (TextView) itemView.findViewById(R.id.text_books_title);
            textAuthor = (TextView) itemView.findViewById(R.id.text_books_author);
            textDescription = (TextView) itemView.findViewById(R.id.text_books_description);
            imageBackground = (ImageView) itemView.findViewById(R.id.image_background);
        }
    }
}