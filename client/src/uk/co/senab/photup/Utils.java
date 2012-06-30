package uk.co.senab.photup;

import java.io.FileNotFoundException;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

public class Utils {

	public static Bitmap drawViewOntoBitmap(View view) {
		Bitmap image = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(image);
		view.draw(canvas);
		return image;
	}

	public static Animation createScaleAnimation(View view, int parentWidth, int parentHeight, int toX, int toY) {
		// Difference in X and Y
		final int diffX = toX - view.getLeft();
		final int diffY = toY - view.getTop();

		// Calculate actual distance using pythagors
		float diffDistance = FloatMath.sqrt((toX * toX) + (toY * toY));
		float parentDistance = FloatMath.sqrt((parentWidth * parentWidth) + (parentHeight * parentHeight));

		ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.ABSOLUTE, diffX,
				Animation.ABSOLUTE, diffY);
		scaleAnimation.setFillAfter(true);
		scaleAnimation.setInterpolator(new DecelerateInterpolator());
		scaleAnimation.setDuration(Math.round(diffDistance / parentDistance
				* Constants.SCALE_ANIMATION_DURATION_FULL_DISTANCE));

		return scaleAnimation;
	}

	// And to convert the image URI to the direct file system path of the image
	// file
	public static String getPathFromContentUri(ContentResolver cr, Uri contentUri) {

		String returnValue = null;

		if (ContentResolver.SCHEME_CONTENT.equals(contentUri.getScheme())) {
			// can post image
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = cr.query(contentUri, proj, null, null, null);

			if (null != cursor) {
				final int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

				if (cursor.moveToFirst()) {
					returnValue = cursor.getString(column_index);
				}
				cursor.close();
			}
		} else if (ContentResolver.SCHEME_FILE.equals(contentUri.getScheme())) {
			returnValue = contentUri.getPath();
		}

		return returnValue;
	}

	public static Bitmap resizeBitmap(final ContentResolver resolver, final Uri uri, final int MAX_DIM)
			throws FileNotFoundException {

		// Get original dimensions
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeStream(resolver.openInputStream(uri), null, o);
		} catch (SecurityException se) {
			se.printStackTrace();
			return null;
		}

		final int origWidth = o.outWidth;
		final int origHeight = o.outHeight;

		// Holds returned bitmap
		Bitmap bitmap;

		o.inJustDecodeBounds = false;
		o.inDither = false;
		o.inScaled = false;
		o.inPurgeable = true;
		o.inInputShareable = true;

		if (origWidth > MAX_DIM || origHeight > MAX_DIM) {
			int k = 1;
			int tmpHeight = origHeight, tmpWidth = origWidth;
			while ((tmpWidth / 2) >= MAX_DIM || (tmpHeight / 2) >= MAX_DIM) {
				tmpWidth /= 2;
				tmpHeight /= 2;
				k *= 2;
			}
			o.inSampleSize = k;

			bitmap = BitmapFactory.decodeStream(resolver.openInputStream(uri), null, o);
		} else {
			bitmap = BitmapFactory.decodeStream(resolver.openInputStream(uri), null, o);
		}
		
		if (Constants.DEBUG) {
			Log.d("Utils", "Resized bitmap to: " + bitmap.getWidth() + "x" + bitmap.getHeight());
		}

		return bitmap;
	}

}
