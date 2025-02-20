package com.example.docbaov1613.Art;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docbaov1613.DetailActivity;
import com.example.docbaov1613.R;

import java.util.ArrayList;
import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private Context context;
    private List<Article> articles;

    public ArticleAdapter(Context context) {
        this.context = context;
        this.articles = new ArrayList<>();
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }


    class ArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTextView;
        TextView contentTextView;
        ImageView imageView;
        Article currentArticle;

        ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.article_title);
            contentTextView = itemView.findViewById(R.id.article_content);
            imageView = itemView.findViewById(R.id.article_image);
            itemView.setOnClickListener(this);
        }

        void bind(Article article) {
            currentArticle = article;
            titleTextView.setText(article.getTitle());

            String content = article.getContent();
            if (content != null && content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
            contentTextView.setText(content);

            // Load image using Glide
            String imageUrl = article.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                        .error(R.drawable.error_image) // Error image if loading fails
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.logodoan); // Default image if no URL provided
            }
        }

        @Override
        public void onClick(View v) {
            if (currentArticle != null) {
                openDetailActivity(currentArticle);
                Toast.makeText(v.getContext(), "Bạn đã click vào: " + currentArticle.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openDetailActivity(Article article) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("article_id", article.getArticleId());
        intent.putExtra("article_title", article.getTitle());
        intent.putExtra("article_content", article.getContent());
        intent.putExtra("article_category", article.getCategory());
        intent.putExtra("article_date", article.getPublicationDate());
        intent.putExtra("article_tags", article.getTags());
        intent.putExtra("article_image_url", article.getImageUrl());

        context.startActivity(intent);
    }

}