package RecyclerViewUtilitiesPackage;

import android.view.View;

/**
 * Created by aravinda on 1/2/18.
 */
public interface ItemClickListener {
    void onClick(View v, int position);
    void onLongClick(View v, int position);
}
