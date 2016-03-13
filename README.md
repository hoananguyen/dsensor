# dsensor

<b>USAGE</b>

Call one of the <code>DSensorManager startDProcessedSensor</code> or <code>startDSensor</code> methods in <code>onResume</code> and <code>stopDSensor</code> in <code>onPause</code>

For compass app you just need the code below. The library will take care of the type of device and/or requested orientation.
<pre><code class="language-java">
@Override
protected void onResume() {
    super.onResume();
    DSensorManager.startDProcessedSensor(this, DProcessedSensor.TYPE_COMPASS_FLAT_ONLY, 
              new DProcessedEventListener() {
                        @Override
                        public void onProcessedValueChanged(DSensorEvent dSensorEvent) {
                            // update UI
                            // dSensorEvent.values[0] is the azimuth.
                        }
                    });
}

@Override
protected void onPause() {
    DSensorManager.stopDSensor();
    super.onPause();
}
</code></pre>

For more usage check out a sample project at https://github.com/hoananguyen/dsensor-sample 

<b>Android Studio</b>

In your project import this library as new module and in the build.graddle 
<pre><code class="language-java">
dependencies {
  ...
  compile project(':dsensor');
}
</code></pre>
                  
