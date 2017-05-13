package br.com.versalius.carona.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.versalius.carona.R;

/**
 * Created by Giovanne on 03/12/2016.
 */

public class GalleryAdapter extends BaseAdapter {

    private Context context;
    private List<ImageView> images;

    public GalleryAdapter(Context context, List<ImageView> images) {
        this.context = context;
        if (images != null) {
            this.images = images;
        } else {
            this.images = new ArrayList<>();
        }
        createAddButtonItem();
    }

    /**
     * Adiciona o botão de adicionar imagens na galeria, no fim da grid
     *
     * @return
     */
    private Bitmap createAddButtonItem() {
        Bitmap addButtom = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);

        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(addButtom);
        imageView.setAdjustViewBounds(true);
        imageView.setTag("addButton");
        imageView.setFocusable(false);

        images.add(images.size(), imageView);

        return null;
    }

    public void addItem(ImageView image) {
        images.add(images.size() - 1, image);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int i) {
        return images.get(i);
    }

    @Override
    public long getItemId(int i) {
        return images.get(i).getId();
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        if(images.get(i).getTag() != null && images.get(i).getTag().equals("addButton")){
            images.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Toast.makeText(context,"Adicionar",Toast.LENGTH_LONG).show();

                    ImageView imageView = new ImageView(context);
                    imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.ic_empty_ride_list));
                    imageView.setAdjustViewBounds(true);
                    imageView.setFocusable(false);

                    images.add(0, imageView);
                    view.clearFocus();
                    notifyDataSetChanged();
                }
            });
        } else {
//            images.get(i).setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    images.remove(view);
//                    notifyDataSetChanged();
//                    return true;
//                }
//            });
            images.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setFocusable(false);
                    images.get(images.indexOf(view)).clearFocus();
                    images.remove(view);
                    notifyDataSetChanged();
                    images.get(images.size()-1).requestFocus();
                }
            });
        }

        return images.get(i);
    }
}
