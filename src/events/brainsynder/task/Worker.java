package events.brainsynder.task;

import simple.brainsynder.nbt.StorageTagCompound;

public class Worker implements IWork {

	private int cooldown;
    private int maxCooldown;

	/**
     * What will be ran in the {@link #doWork()} and {@link #workDone()} methods
	 */
	private Runnable doWork, workDone;

	/**
	 * Create an instance of the IWork which
	 * will do work
	 * 
	 * @param maxCooldown
	 *            The maximum number of ticks until work is done
	 * @param doWork
	 *            What will happen every tick
	 * @param workDone
	 *            What will happen when the work is completed
	 */
	public Worker(int maxCooldown, Runnable doWork, Runnable workDone) {
		this.cooldown = 0;
		this.maxCooldown = maxCooldown;
		this.doWork = doWork;
		this.workDone = workDone;
	}

	/**
	 * Set the maximum number of ticks until work is done
	 * 
	 * @param maxCooldown
	 *            The maximum number of ticks until work is done
	 * @return The updated {@link Worker}
	 */
	public Worker setMaxCooldown(int maxCooldown) {
		this.maxCooldown = maxCooldown;
		return this;
	}

	/**
	 * Get the cooldown
	 */
	@Override
	public int getWorkDone() {
		return this.cooldown;
	}

	/**
	 * Get the cooldown cap or max cooldown
	 */
	@Override
	public int getMaxWork() {
		return this.maxCooldown;
	}

	/**
	 * To be called every time you want it to update
	 */
	@Override
	public void doWork() {
		this.cooldown++;
		this.cooldown %= this.maxCooldown; // Caps the cooldown to the max cooldown
		this.doWork.run();
		if (this.cooldown == 0)
			workDone();
	}

	/**
	 * To be used internally, called when the work has been done
	 */
	@Override
	public void workDone() {
		this.workDone.run();
	}

	/**
	 * Write all data to an {@link StorageTagCompound}
	 */
	public StorageTagCompound serializeNBT() {
		StorageTagCompound nbt = new StorageTagCompound();
		nbt.setInteger("cooldown", this.cooldown);
		nbt.setInteger("maxCooldown", this.maxCooldown);
		return nbt;
	}

	/**
	 * Read all data from an {@link StorageTagCompound}
	 */
	public void deserializeNBT(StorageTagCompound nbt) {
		this.cooldown = nbt.getInteger("cooldown");
		this.maxCooldown = nbt.getInteger("maxCooldown");
	}

}