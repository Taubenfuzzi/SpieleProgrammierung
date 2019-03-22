package de.greenfootdevz.charactereditor.actor;

import de.greenfootdevz.charactereditor.GreenfootImageExtended;
import greenfoot.*;

import java.awt.*;

public class BodyPart extends Actor implements Cloneable {


    private final GreenfootImage[] images;
    private final GreenfootImage[] prevImages;
    private boolean isPreview;
    private String name;
    private int currentImage;

    public void setPreview(boolean val) {
        isPreview = val;
        setCurrentImage();
    }

    public String getName() {
        return name;
    }

    public BodyPart(String name, GreenfootImage[] images, GreenfootImage[] prevImages, boolean isPreview) throws IllegalArgumentException {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("images must be an array with at least one element");
        }
        if (images.length != prevImages.length) {
            throw new IllegalArgumentException("unequal amount of prev images: " + images.length + " " + prevImages.length);
        }
        this.name = name;
        this.images = images;
        this.prevImages = prevImages;
        this.isPreview = isPreview;
        currentImage = 0;
        setCurrentImage();
    }

    public BodyPart(String name, GreenfootImage[] images, GreenfootImage[] prevImages, boolean isPreview, int currentImage) throws IllegalArgumentException {
        this(name, images, prevImages, isPreview);
        this.currentImage = currentImage;
        setCurrentImage();
    }

    public void act() {

    }

    public void previous() {
        currentImage--;
        if (currentImage < 0) {
            currentImage = images.length - 1;
        }
        setCurrentImage();
    }

    public void next() {
        currentImage++;
        if (currentImage == images.length) {
            currentImage = 0;
        }
        setCurrentImage();
    }

    private void setCurrentImage() {
        if (isPreview) {
            setImage(prevImages[currentImage]);
        } else {
            setImage(images[currentImage]);
        }
    }

    @Override
    public BodyPart clone() {
        return new BodyPart(this.name, this.images, this.prevImages, this.isPreview, this.currentImage);
    }

    public GreenfootImageExtended getCurrentImage() {
        return ((GreenfootImageExtended)images[currentImage]);
    }
}