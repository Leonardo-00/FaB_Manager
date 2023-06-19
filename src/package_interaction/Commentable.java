package package_interaction;

import Utils.Exceptions.CommentNotFoundException;

public interface Commentable {
    void addComment(String user, String comment);

    void responseComment(String user, String answer, Comment comment) throws CommentNotFoundException;
}
