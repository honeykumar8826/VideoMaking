package com.videoMaking.modal;

public class ShareReuseModal {
    private String imgUrl;
    private String userName;
    private String name;

    public ShareReuseModal(String imgUrl, String userName, String name) {
        this.imgUrl = imgUrl;
        this.userName = userName;
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

/*
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
*/

    public String getUserName() {
        return userName;
    }

  /*  public void setUserName(String userName) {
        this.userName = userName;
    }*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
