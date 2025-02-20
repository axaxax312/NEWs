package com.example.docbaov1613;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docbaov1613.Adapter.CommentAdapter;
import com.example.docbaov1613.Api.ApiClient;
import com.example.docbaov1613.Api.ApiService;
import com.example.docbaov1613.Art.Article;
import com.example.docbaov1613.Art.ArticleAdapter;
import com.example.docbaov1613.Model.Comment;
import com.example.docbaov1613.ViewModels.ArticleViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private TextView titleTextView, contentTextView, categoryTextView, dateTextView, tagsTextView;
    private ImageView imageView;
    private RecyclerView commentRecyclerView;
    private CommentAdapter commentAdapter;
    private EditText commentInput;
    private Button submitCommentButton, saveArticleButton;
    private ApiService apiService;
    private int articleId = -1;
    private String category;
    private ArticleViewModel articleViewModel;
    private ArticleAdapter articleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        // Initialize ApiService
        apiService = ApiClient.getClient().create(ApiService.class);


        // Khởi tạo RecyclerView cho bài viết gợi ý
        RecyclerView recyclerViewSuggest = findViewById(R.id.recycler_view_suggest);
        recyclerViewSuggest.setLayoutManager(new LinearLayoutManager(this));

        articleAdapter = new ArticleAdapter(this);
        recyclerViewSuggest.setAdapter(articleAdapter);

        // Khởi tạo ArticleViewModel
        articleViewModel = new ViewModelProvider(this).get(ArticleViewModel.class);


        // Mapping UI components
        titleTextView = findViewById(R.id.titleTextView);
        contentTextView = findViewById(R.id.contentTextView);
        categoryTextView = findViewById(R.id.categoryTextView);
        dateTextView = findViewById(R.id.dateTextView);
        tagsTextView = findViewById(R.id.tagsTextView);
        imageView = findViewById(R.id.imageView);
        commentInput = findViewById(R.id.commentInput);
        submitCommentButton = findViewById(R.id.submitCommentButton);
        saveArticleButton = findViewById(R.id.saveArticleButton);
        commentRecyclerView = findViewById(R.id.commentsRecyclerView);

        // Setup RecyclerView for comments
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(new ArrayList<>());
        commentRecyclerView.setAdapter(commentAdapter);

        // Get data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            articleId = intent.getIntExtra("article_id", -1);
            String title = intent.getStringExtra("article_title");
            String content = intent.getStringExtra("article_content");
            category = intent.getStringExtra("article_category");
            String date = intent.getStringExtra("article_date");
            String tags = intent.getStringExtra("article_tags");
            String imageUrl = intent.getStringExtra("article_image_url");

            // Check if articleId is valid
            if (articleId == -1) {
                Toast.makeText(this, "Invalid article ID", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Display data on UI
            titleTextView.setText(title);
            contentTextView.setText(content);
            categoryTextView.setText("Category: " + category);
            dateTextView.setText("Publication Date: " + date);
            tagsTextView.setText("Tags: " + tags);

            // Load image if available
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.logodoan);
            }


            // Lấy category từ Intent hoặc Bundle
            String category = getIntent().getStringExtra("article_category");
            if (category != null) {
                articleViewModel.loadSuggestedArticles(category);
                observeSuggestedArticles(category);
            }



            // Simulate reading count update after delay
            new Handler().postDelayed(() -> updateReadCount(articleId), 10000); // 10 seconds delay for example

            // Load comments
            loadComments(articleId);
        }

        // Submit Comment Button Click Listener
        submitCommentButton.setOnClickListener(view -> {
            String commentText = commentInput.getText().toString().trim();
            int userId = getUserId(); // Lấy UserId từ SharedPreferences
            if (!commentText.isEmpty()) {
                submitComment(articleId, userId, commentText);
            } else {
                Toast.makeText(DetailActivity.this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            }
        });

        // Save Article Button Click Listener
        saveArticleButton.setOnClickListener(view -> {
            int userId = getUserId(); // Lấy UserId từ SharedPreferences
            if (userId != -1 && articleId != -1) {
                saveArticle(userId, articleId);
            } else {
                Toast.makeText(DetailActivity.this, "Unable to save article", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments(int articleId) {
        Call<List<Comment>> call = apiService.getComments(articleId);

        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> comments = response.body();
                    commentAdapter.setComments(comments);
                } else {
                    Log.e("DetailActivity", "Failed to load comments");
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Log.e("DetailActivity", "Error: " + t.getMessage());
            }
        });
    }

    private void submitComment(int articleId, int userId, String commentText) {
        Call<Void> call = apiService.submitComment(articleId, userId, commentText);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "Comment submitted successfully", Toast.LENGTH_SHORT).show();
                    // Clear input field and reload comments
                    commentInput.setText("");
                    loadComments(articleId);
                } else {
                    Toast.makeText(DetailActivity.this, "Failed to submit comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReadCount(int articleId) {
        Call<Void> call = apiService.updateViewCount(articleId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // View count updated successfully
                    Log.d("DetailActivity", "View count updated successfully for articleId: " + articleId);
                } else {
                    Log.e("DetailActivity", "Failed to update view count for articleId: " + articleId);
                    Toast.makeText(DetailActivity.this, "Failed to update view count", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DetailActivity", "Error: " + t.getMessage(), t);
                Toast.makeText(DetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveArticle(int userId, int articleId) {
        Call<Void> call = apiService.saveArticle(articleId, userId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "Article saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DetailActivity.this, "Failed to save article", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private int getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getInt("UserId", -1);
    }
    private void observeSuggestedArticles(String category) {
        articleViewModel.getSuggestedArticles(category).observe(this, new Observer<List<Article>>() {
            @Override
            public void onChanged(List<Article> articles) {
                articleAdapter.setArticles(articles);
            }
        });
    }
}