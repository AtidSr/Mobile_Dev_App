package com.somesteak.finalm;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookHolder> implements Filterable {
    private Context context;
    private ArrayList<Book> books;
    private ArrayList<Book> fullBookList;
    public BookAdapter(Context context, ArrayList < Book > books) {
        this.context = context;
        this.books = books;
        this.fullBookList = new ArrayList<>(books);
    }


    @NonNull
    @Override
    public BookHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row, viewGroup, false);
        BookHolder holder = new BookHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BookHolder bookHolder, int i) {
        Book book = books.get(i);
        bookHolder.setDetails(book);
        if(fullBookList.size() == 0) {
            this.fullBookList = new ArrayList<>(books);
        }
   }

    @Override
    public int getItemCount() {
        return books.size();
    }

    @Override
    public Filter getFilter() {
        return fillterBookList;
    }

    private Filter fillterBookList = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Book> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length() == 0) {
                filteredList.addAll(fullBookList);
            } else {
                String fillterPattern = constraint.toString().toLowerCase().trim();

                for(Book b: fullBookList) {

                    if(b.getTitle().toLowerCase().contains(fillterPattern)){
                        filteredList.add(b);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            books.clear();
            books.addAll((List) results.values);
            books.forEach(item -> Log.d("asd",item.getTitle() + " " + item.getAuthor()));
            notifyDataSetChanged();
        }
    };

    public List<Book> fullBookList () {
        return  this.fullBookList;
    }


    public class BookHolder extends RecyclerView.ViewHolder {
        private TextView cardTitle, cardAuthor, cardVolume, cardOwned;
        private ImageView imageView;
        public BookHolder(View itemView) {
            super(itemView);
            cardTitle = itemView.findViewById(R.id.cardTitle);
            cardAuthor = itemView.findViewById(R.id.cardAuthor);
            cardVolume = itemView.findViewById(R.id.cardVolume);
            cardOwned = itemView.findViewById(R.id.cardOwned);
            imageView = itemView.findViewById(R.id.cardImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), EditBook.class);
                    i.putExtra("title", books.get(getAdapterPosition()).getTitle());
                    i.putExtra("author",  books.get(getAdapterPosition()).getAuthor());
                    i.putExtra("volume",  books.get(getAdapterPosition()).getLatest());
                    i.putExtra("owned",  books.get(getAdapterPosition()).getOwned());
                    i.putExtra("image",  books.get(getAdapterPosition()).getImage());
//                    Drawable drawable=imageView.getDrawable();
//                    Bitmap bitmap= ((BitmapDrawable)drawable).getBitmap();
//                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
//                    byte[] b = byteArray.toByteArray();
//                    i.putExtra("image", b);
                    v.getContext().startActivity(i);

//                    books.remove(getAdapterPosition());
//                    notifyItemRemoved(getAdapterPosition());
//                    notifyItemRangeChanged(getAdapterPosition(), books.size());
                }
            });
        }

        public void setDetails(Book book) {
            cardTitle.setText("Title: " + book.getTitle());
            cardAuthor.setText("Author: " + book.getAuthor());
            cardVolume.setText("Latest: " + book.getLatest());
            cardOwned.setText("Owned: " + book.getOwned());
            Glide.with(itemView.getContext())
                    .load(book.getImage())
                    .into(imageView);

        }

    }
}
