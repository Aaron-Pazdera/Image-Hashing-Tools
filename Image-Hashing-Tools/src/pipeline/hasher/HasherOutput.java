package pipeline.hasher;

import hash.ImageHash;

@FunctionalInterface
public interface HasherOutput {
	public abstract void store(ImageHash hash);
}
