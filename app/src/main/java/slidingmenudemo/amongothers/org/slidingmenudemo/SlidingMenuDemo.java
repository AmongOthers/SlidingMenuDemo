package slidingmenudemo.amongothers.org.slidingmenudemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.amongothers.slidingmenu.SlidingMenuLayout;

public class SlidingMenuDemo extends Activity {
  TextView mPercentTv;
  Handler mHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.content_frame);

    Button btn = (Button) findViewById(R.id.btn);
    btn.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(SlidingMenuDemo.this, "按钮被点击", Toast.LENGTH_SHORT).show();
      }
    });
    mPercentTv = (TextView) findViewById(R.id.percent);
    mHandler = new Handler();
    final SlidingMenuLayout slidingMenuLayout = new SlidingMenuLayout(this);
    View menu = this.getLayoutInflater().inflate(R.layout.menu, null);
    Button menubtn = (Button) menu.findViewById(R.id.menubtn);
    menubtn.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(SlidingMenuDemo.this, "菜单按钮被点击", Toast.LENGTH_SHORT).show();
      }
    });
    slidingMenuLayout.init(this, menu);
    slidingMenuLayout.setMenuListener(new SlidingMenuLayout.MenuListener() {
      @Override
      public void onPercentChanged(final float percent) {
        mHandler.post(new Runnable() {
          @Override
          public void run() {
            mPercentTv.setText(String.format("percent: %f", percent));
          }
        });
      }
    });
  }

}
