package com.example.blogosphere;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blogosphere.database.CommentModal;
import com.example.blogosphere.database.DBHelper;
import com.example.blogosphere.database.UserModel;

import java.util.ArrayList;
import java.util.List;

public class ViewComment extends AppCompatActivity {

    ListView listView;
    private EditText comments;
    private Button add;
    private ImageView close;
    private DBHelper myDB;
    private Context context;
    private List<CommentModal> allComments;

    UserModel user;
    String articleId;
    int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comment);

        context = this;
        myDB = new DBHelper(context);

        // Get login user object
//        if (user == null) {
//            Intent i = getIntent();
//            user = (UserModel) i.getSerializableExtra("UserObject");
//        }

        userID = getIntent().getIntExtra("UserID",0);
        articleId = getIntent().getStringExtra("StoryID");
        user = myDB.getUserbyID(userID);

        listView = findViewById(R.id.listView);
        add = findViewById(R.id.buttonAdd);
        comments = findViewById(R.id.editTextComment);
        close = findViewById(R.id.imageViewClose);

        //get all comments for a article
        allComments = new ArrayList<>();
        allComments = myDB.getALLComments(articleId);
        CommentAdapter adapter = new CommentAdapter(context, R.layout.single_row, allComments, myDB, userID);
        listView.setAdapter(adapter);

        //comment add button
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //int userId = 1;
                //int blogId = 1;

                String userComments = comments.getText().toString();

                if (userComments.length() > 0){

                    long date = System.currentTimeMillis();
                    CommentModal comment = new CommentModal(userID, Integer.parseInt(articleId), userComments, date);
                    myDB.addComment(comment);
                    Intent addCommentIntent = new Intent(ViewComment.this, ViewComment.class);
                    addCommentIntent.putExtra("UserID", userID);
                    addCommentIntent.putExtra("id", String.valueOf(comment.getComId()));
                    addCommentIntent.putExtra("StoryID", articleId);
                    startActivity(addCommentIntent);
                    overridePendingTransition(0, 0);
                }else {
                    Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                }

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CommentModal comment = allComments.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                        builder1.setTitle("Are you sure to delete the comment?");
                        builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myDB.deleteComment(comment.getComId());
                                Toast.makeText(context, "Comment Deleted", Toast.LENGTH_SHORT).show();
                                Intent deleteIntent = new Intent(context, ViewComment.class);
                                deleteIntent.putExtra("UserID", userID);
                                deleteIntent.putExtra("id", String.valueOf(comment.getComId()));
                                deleteIntent.putExtra("StoryID", articleId);
                                startActivity(deleteIntent);
                                overridePendingTransition(0, 0);
                            }
                        });

                        builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent noIntent = new Intent(context, ViewComment.class);
                                startActivity(noIntent);
                            }
                        });
                        builder1.show();
                    }
                });
                builder.setNeutralButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent addCommentintent = new Intent(ViewComment.this, AddComment.class);
                        addCommentintent.putExtra("UserID", userID);
                        addCommentintent.putExtra("id", String.valueOf(comment.getComId()));
                        addCommentintent.putExtra("StoryID", articleId);
                        startActivity(addCommentintent);
                        overridePendingTransition(0, 0);
                    }
                });
                builder.show();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewblogIntent = new Intent(context, ViewBlogPost.class);
                viewblogIntent.putExtra("UserID", userID);
                viewblogIntent.putExtra("StoryID", articleId);
                startActivity(viewblogIntent);
            }
        });
    }
}

class CommentAdapter extends ArrayAdapter<CommentModal> {
    private Context context;
    private int resource;
    List<CommentModal> comments;
    DBHelper myDB;
    int userID;

    CommentAdapter(Context context, int resource, List<CommentModal> comments, DBHelper myDB, int userID) {
        super(context, resource, comments);
        this.context = context;
        this.resource = resource;
        this.comments = comments;
        this.myDB = myDB;
        this.userID = userID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(resource, parent, false);

        ImageView userImage = row.findViewById(R.id.imageViewUserPhoto);
        TextView username = row.findViewById(R.id.tv_username);
        TextView commentView = row.findViewById(R.id.comment);

        CommentModal comment = comments.get(position);

        userImage.setImageBitmap(myDB.getUserImageById(comment.getUserId()));
        username.setText(myDB.getUserNameById(comment.getUserId()));
        commentView.setText(comment.getComment());

        return row;
    }
}