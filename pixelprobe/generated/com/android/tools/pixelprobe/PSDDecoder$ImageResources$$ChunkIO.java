package com.android.tools.pixelprobe;

import com.android.tools.chunkio.RangedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

final class PSDDecoder$ImageResources$$ChunkIO {
    static PSDDecoder.ImageResources read(RangedInputStream in, LinkedList<Object> stack) throws IOException {
        PSDDecoder.ImageResources imageResources = new PSDDecoder.ImageResources();
        if (stack == null) stack = new LinkedList<Object>();
        stack.addFirst(imageResources);

        int size = 0;
        long byteCount = 0;

        imageResources.length = in.readInt() & 0xffffffffL;
        imageResources.blocks = new ArrayList<PSDDecoder.ImageResourceBlock>();
        byteCount = imageResources.length;
        in.pushRange(byteCount);
        PSDDecoder.ImageResourceBlock imageResourceBlock;
        while (in.available() > 0) {
            imageResourceBlock = PSDDecoder$ImageResourceBlock$$ChunkIO.read(in, stack);
            imageResources.blocks.add(imageResourceBlock);
        }
        in.popRange();

        stack.removeFirst();
        return imageResources;
    }
}