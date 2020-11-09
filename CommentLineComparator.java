import java.util.Comparator;

public class CommentLineComparator implements Comparator<Comment> {
    public int compare(Comment cmnt1, Comment cmnt2) {
        return cmnt1.getLine() - cmnt2.getLine();
    }
}