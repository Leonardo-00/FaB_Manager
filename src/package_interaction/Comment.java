package package_interaction;


import java.util.*;

public class Comment implements Comparable<Comment>{
    private final String user;
    private final String comment;
    private final Date dateOfCreation;
    private final ArrayDeque<Comment> replies;

    public Comment(String user, String comment) {
        this.user = user;
        this.comment = comment;
        this.dateOfCreation = Calendar.getInstance().getTime();
        this.replies = new ArrayDeque<>();
    }

    public Comment(String user, String comment, Date dateOfCreation) {
        this.user = user;
        this.comment = comment;
        this.dateOfCreation = dateOfCreation;
        this.replies = new ArrayDeque<>();
    }

    public String getUser() {
        return user;
    }
    public String getComment() {
        return comment;
    }
    public Date getDateOFCreation(){
        return dateOfCreation;
    }
    
    public Queue<Comment> getAnswers() {
        return replies;
    }

    public void replyComment(Comment answer){
        replies.add(answer);
    }

    public ArrayDeque<Comment> getAllReplies(){
        return new ArrayDeque<>(replies);
    }

    public int compareTo(Comment c) {
        return dateOfCreation.compareTo(c.dateOfCreation);
    }
      
}
