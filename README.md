# MemoryAlbum
[Final Demo PPT](https://github.com/SuminHan/MemoryAlbum/blob/master/CS409_Final_DEMO_Presentation.pdf)

An effective way to manage the pictures into memory.Here's the Demo Video:

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/bCB6etWdfoY/0.jpg)](https://www.youtube.com/watch?v=bCB6etWdfoY)


## Introduction
There are only pictures when the time passes by. Would there be an application that manages the memory efficiently and visually?

- MS Emotion API
- Categorize emotion
- Visualize using Graph

## Features
![alt text](https://github.com/SuminHan/MemoryAlbum/blob/master/1.png) ![alt text](https://github.com/SuminHan/MemoryAlbum/blob/master/2.png) ![alt text](https://github.com/SuminHan/MemoryAlbum/blob/master/3.png) ![alt text](https://github.com/SuminHan/MemoryAlbum/blob/master/4.png) ![alt text](https://github.com/SuminHan/MemoryAlbum/blob/master/5.png)

## Categorization
MS API: Original 8 categories → Convert into 5 categories

- Anger, Contempt, Disgust → Anger
- Fear, Sadness → Sadness
- Neutral → Neutral
- Happiness → Happiness
- Surprise → Surprise

Then, normalize the vector.

(If I knew PCA at this time, I may have applied this approach first rather than manually lower the dimension of vector.)

## Mean value
Emotion vector v_i  with 5th dimension, |v_i| = 1

If there’s n faces, there are v_1, v_2, … v_n  emotion vectors.

μ = mean(v_1, v_2, … v_n)

Similarity: cosine sim(A, B) = A·B / (|A|·|B|)

Familiarity = Deviation

Color ⇒ RGB and interpolate value using familiarity.

## Scala value
float val = (float)(im.FACE_ANGER * -100 + im.FACE_FEAR * -50 + im.FACE_NEUTRAL * 0 + im.FACE_HAPPINESS * 50 + im.FACE_SURPRISE * 100);

## GeoCode & Recording
- Exif Data
  - latitude, longitude→ Convert to real address (Geocoder)
  - datetime → shooting time (sorting)
- Recording
  - Record message to each post(instead of typing)

## System Architecture
- CRUD sqlite
- Upload: Detect Face → Analyze mean value → get Exif info → Copy image in package storage

## Process
- Firebase… web image storage
- Face detection
- MP Android Chart
- Menu design
- Exif data, list view
- Add more views
- Album

## User Feedback
- Happiness, Neutral are quite good, but Anger, Sadness are bad. Mostly the pictures are in Neutral.
- The pictures with many faces are almost Neutral or Happiness.
- Better if I target to make a personal album or a diary would be more perspective.

## Future Work
- Auto filter
- Photo Sticker
- Private diary, security↑
- Voice emotion detection
