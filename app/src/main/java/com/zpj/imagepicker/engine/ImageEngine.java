/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zpj.imagepicker.engine;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

public interface ImageEngine {

    /**
     * Load thumbnail of a static image resource.
     *
     * @param context     Context
     * @param imageView   ImageView widget
     * @param uri         Uri of the loaded image
     */
    void loadThumbnail(Context context, ImageView imageView, Uri uri);

    /**
     * Load thumbnail of a gif image resource. You don't have to load an animated gif when it's only
     * a thumbnail tile.
     *
     * @param context     Context
     * @param imageView   ImageView widget
     * @param uri         Uri of the loaded image
     */
    void loadGifThumbnail(Context context, ImageView imageView, Uri uri);

    /**
     * Load a gif image resource.
     *
     * @param context   Context
     * @param imageView ImageView widget
     * @param uri       Uri of the loaded image
     */
    void loadGifImage(Context context, ImageView imageView, Uri uri);

    /**
     * Whether this implementation supports animated gif.
     * Just knowledge of it, convenient for users.
     *
     * @return true support animated gif, false do not support animated gif.
     */
    boolean supportAnimatedGif();
}