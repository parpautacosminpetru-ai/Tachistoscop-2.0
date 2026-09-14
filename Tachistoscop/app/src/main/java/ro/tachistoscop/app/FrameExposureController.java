package ro.tachistoscop.app;

import android.view.Choreographer;

final class FrameExposureController implements Choreographer.FrameCallback {
    private long durationNs;
    private long startFrameNs;
    private Runnable completion;
    private boolean running;

    void start(long durationMs, Runnable completion) {
        cancel();
        this.durationNs = Math.max(1L, durationMs) * 1_000_000L;
        this.startFrameNs = 0L;
        this.completion = completion;
        this.running = true;
        Choreographer.getInstance().postFrameCallback(this);
    }

    void cancel() {
        if (running) {
            Choreographer.getInstance().removeFrameCallback(this);
        }
        running = false;
        startFrameNs = 0L;
        completion = null;
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!running) return;
        if (startFrameNs == 0L) startFrameNs = frameTimeNanos;

        if (frameTimeNanos - startFrameNs >= durationNs) {
            Runnable done = completion;
            running = false;
            completion = null;
            if (done != null) done.run();
        } else {
            Choreographer.getInstance().postFrameCallback(this);
        }
    }
}
