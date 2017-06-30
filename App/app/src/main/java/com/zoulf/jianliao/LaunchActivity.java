package com.zoulf.jianliao;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Property;
import android.view.View;
import com.zoulf.common.app.MyActivity;
import com.zoulf.factory.persistence.Account;
import com.zoulf.jianliao.activities.AccountActivity;
import com.zoulf.jianliao.activities.MainActivity;
import com.zoulf.jianliao.frags.assist.PermissionFragment;
import net.qiujuer.genius.res.Resource;
import net.qiujuer.genius.ui.compat.UiCompat;

public class LaunchActivity extends MyActivity {

  ColorDrawable mBgDrawable;

  @Override
  protected int getContentLayoutId() {
    return R.layout.activity_launch;
  }

  @Override
  protected void initWidget() {
    super.initWidget();
    // 拿到根布局
    View root = findViewById(R.id.activity_launch);
    // 获取颜色
    int color = UiCompat.getColor(getResources(), R.color.colorPrimary);
    // 创建一个Drawable
    ColorDrawable colorDrawable = new ColorDrawable(color);
    // 设置给背景
    root.setBackground(colorDrawable);
    mBgDrawable = colorDrawable;
  }

  @Override
  protected void initData() {
    super.initData();
    // 动画进入到50%等待PushId获取到
    startAnim(0.5f, new Runnable() {
      @Override
      public void run() {
        // 检查等待状态
        waitPushReceiverId();
      }
    });
  }

  /**
   * 等待个推框架对我们的PushId设置好值
   */
  private void waitPushReceiverId() {

    if (Account.isLogin()) {
      // 已经登录的情况下，判断是否已经绑定
      // 如果没有绑定则等待广播接收器进行绑定
      if (Account.isBind()) {
        skip();
        return;
      }
    } else {
      // 没有登录
      // 如果拿到了PushId，没有登录时不能绑定PushId
      if (!TextUtils.isEmpty(Account.getPushId())) {
        // 跳转
        skip();
        return;
      }
    }
    // 循环等待
    getWindow().getDecorView().postDelayed(new Runnable() {
      @Override
      public void run() {
        waitPushReceiverId();
      }
    }, 500);

  }

  /**
   * 在跳转之前需要把剩下的50%进行完成
   */
  private void skip() {

    startAnim(1f, new Runnable() {
      @Override
      public void run() {
        reallySkip();
      }
    });
  }

  /**
   * 真实的跳转
   */
  private void reallySkip() {
    // 权限检测
    if (PermissionFragment.haveAll(this, getSupportFragmentManager())) {
      // 检查跳转到主页还是登录
      if (Account.isLogin()) {
        MainActivity.show(this);
        finish();
      } else {
        AccountActivity.show(this);
      }

    }
  }

  private void startAnim(float endProgress, final Runnable endCallback) {
    // 获取一个最终的颜色
    int finalColor = Resource.Color.WHITE; // UiCompat.getColor(getResources(), R.color.white);
    // 运算当前进度的颜色
    ArgbEvaluator evaluator = new ArgbEvaluator();
    int endColor = (int) evaluator.evaluate(endProgress, mBgDrawable.getColor(), finalColor);
    // 构建一个属性动画
    ValueAnimator valueAnimator = ObjectAnimator.ofObject(this, property, evaluator, endColor);
    valueAnimator.setDuration(1500); // 时间
    valueAnimator.setIntValues(mBgDrawable.getColor(), endColor); // 开始结束值
    valueAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        // 结束时触发
        endCallback.run();
      }
    });
    valueAnimator.start();
  }

  private final Property<LaunchActivity, Object> property = new Property<LaunchActivity, Object>(
      Object.class, "color") {

    @Override
    public Object get(LaunchActivity launchActivity) {
      return mBgDrawable.getColor();
    }

    @Override
    public void set(LaunchActivity object, Object value) {
      object.mBgDrawable.setColor((Integer) value);
    }
  };
}
