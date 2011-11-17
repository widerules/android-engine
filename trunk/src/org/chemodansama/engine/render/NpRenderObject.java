package org.chemodansama.engine.render;

import javax.microedition.khronos.opengles.GL10;

public interface NpRenderObject {
    /**
     * @param gl cannot be {@code null}.
     */
    public void draw(GL10 gl);
    
    /**
     * @param deltaTime time delta in milliseconds.
     */
    public boolean update(long deltaTime);
}
