package io.github.tylerhackett.caf.domain;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity

@NamedQueries({
        @NamedQuery(
                name="AllImages",
                query="select im from Image im where im.approved = true"),
        @NamedQuery(
                name="ImagesForApproval",
                query="select im from Image im where im.approved = false"),
        @NamedQuery(
                name="ImagesByUser",
                query="select im from Image im where im.loader = :username"),
        @NamedQuery(
                name="DeleteImages",
                query="delete from Image im"),
})

public class Image implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    private String caption;

    private String url;

    private Instant timestamp;

    private String loader;

    private boolean approved;

    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("timestamp")
    private List<Comment> comments;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getLoader() {
        return loader;
    }

    public void setLoader(String user) {
        this.loader = user;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }


    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Image() {
        this.comments = new ArrayList<>();
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setImage(this);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
    }
}
