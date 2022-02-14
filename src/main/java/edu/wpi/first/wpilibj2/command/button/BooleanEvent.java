// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj2.command.button;

import static edu.wpi.first.wpilibj.util.ErrorMessages.requireNonNullParam;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.Subsystem;
import java.util.function.BooleanSupplier;

/**
 * This class provides an easy way to link commands to inputs.
 *
 * <p>It is very easy to link a button to a command. For instance, you could link the BooleanEvent button
 * of a joystick to a "score" command.
 *
 * <p>It is encouraged that teams write a subclass of BooleanEvent if they want to have something unusual
 * (for instance, if they want to react to the user holding a button while the robot is reading a
 * certain sensor input). For this, they only have to write the {@link BooleanEvent#get()} method to get
 * the full functionality of the BooleanEvent class.
 *
 * <p>This class is provided by the NewCommands VendorDep
 */
public class BooleanEvent implements BooleanSupplier {
  private final BooleanSupplier m_eventSupplier;

  /**
   * Creates a new BooleanEvent that monitors the given condition.
   *
   * @param eventSupplier the condition the BooleanEvent should monitor.
   */
  public BooleanEvent(BooleanSupplier eventSupplier) {
    m_eventSupplier = eventSupplier;
  }

  /**
   * Creates a new BooleanEvent that is always false. Useful only as a no-arg constructor for
   * subclasses that will be overriding {@link BooleanEvent#get()} anyway.
   */
  public BooleanEvent() {
    m_eventSupplier = () -> false;
  }

  /**
   * Returns whether or not the BooleanEvent's condition is true.
   *
   * <p>This method will be called repeatedly when a command is linked to the BooleanEvent.
   *
   * <p>Functionally identical to {@link BooleanEvent#getAsBoolean()}.
   *
   * @return whether or not the BooleanEvent condition is true.
   */
  public boolean get() {
    return this.getAsBoolean();
  }

/**
   * Returns whether or not the BooleanEvent's condition is true.
   *
   * <p>This method will be called repeatedly when a command is linked to the BooleanEvent.
   *
   * <p>Functionally identical to {@link BooleanEvent#get()}.
   *
   * @return whether or not the BooleanEvent condition is true.
   */
  @Override
  public boolean getAsBoolean() {
    return m_eventSupplier.getAsBoolean();
  }

  /**
   * Starts the given command whenever the BooleanEvent's condition just becomes true.
   *
   * @param command the command to start
   * @param interruptible whether the command is interruptible
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onTrue(final Command command, boolean interruptible) {
    requireNonNullParam(command, "command", "onTrue");

    CommandScheduler.getInstance()
        .addButton(
            new Runnable() {
              private boolean m_pressedLast = get();

              @Override
              public void run() {
                boolean pressed = get();

                if (!m_pressedLast && pressed) {
                  command.schedule(interruptible);
                }

                m_pressedLast = pressed;
              }
            });

    return this;
  }

  /**
   * Starts the given command whenever the BooleanEvent's condition just becomes true. The command is set to be
   * interruptible.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onTrue(final Command command) {
    return onTrue(command, true);
  }

  /**
   * Runs the given runnable whenever the BooleanEvent's condition just becomes true.
   *
   * @param toRun the runnable to run
   * @param requirements the required subsystems
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onTrue(final Runnable toRun, Subsystem... requirements) {
    return onTrue(new InstantCommand(toRun, requirements));
  }

  /**
   * Constantly starts the given command while the condition is true.
   *
   * <p>{@link Command#schedule(boolean)} will be called repeatedly while the condition is true, and
   * will be canceled when the condition becomes false.
   *
   * @param command the command to start
   * @param interruptible whether the command is interruptible
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrueContinuous(final Command command, boolean interruptible) {
    requireNonNullParam(command, "command", "whileTrueContinuous");

    CommandScheduler.getInstance()
        .addButton(
            new Runnable() {
              private boolean m_pressedLast = get();

              @Override
              public void run() {
                boolean pressed = get();

                if (pressed) {
                  command.schedule(interruptible);
                } else if (m_pressedLast) {
                  command.cancel();
                }

                m_pressedLast = pressed;
              }
            });
    return this;
  }

  /**
   * Constantly starts the given command while the condition is true.
   *
   * <p>{@link Command#schedule(boolean)} will be called repeatedly while the condition is true, and
   * will be canceled when the condition becomes false. The command is set to be interruptible.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrueContinuous(final Command command) {
    return whileTrueContinuous(command, true);
  }

  /**
   * Constantly runs the given runnable while the condition is true.
   *
   * @param toRun the runnable to run
   * @param requirements the required subsystems
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrueContinuous(final Runnable toRun, Subsystem... requirements) {
    return whileTrueContinuous(new InstantCommand(toRun, requirements));
  }

  /**
   * Starts the given command when the condition initially becomes true, and ends it when it becomes
   * false, but does not re-start it in-between.
   *
   * @param command the command to start
   * @param interruptible whether the command is interruptible
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrueOnce(final Command command, boolean interruptible) {
    requireNonNullParam(command, "command", "whileTrueOnce");

    CommandScheduler.getInstance()
        .addButton(
            new Runnable() {
              private boolean m_pressedLast = get();

              @Override
              public void run() {
                boolean pressed = get();

                if (!m_pressedLast && pressed) {
                  command.schedule(interruptible);
                } else if (m_pressedLast && !pressed) {
                  command.cancel();
                }

                m_pressedLast = pressed;
              }
            });
    return this;
  }

  /**
   * Starts the given command when the condition initially becomes true, and ends it when it becomes
   * false, but does not re-start it in-between. The command is set to be interruptible.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrueOnce(final Command command) {
    return whileTrueOnce(command, true);
  }

  /**
   * Starts the command when the condition becomes false.
   *
   * @param command the command to start
   * @param interruptible whether the command is interruptible
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onFalse(final Command command, boolean interruptible) {
    requireNonNullParam(command, "command", "onFalse");

    CommandScheduler.getInstance()
        .addButton(
            new Runnable() {
              private boolean m_pressedLast = get();

              @Override
              public void run() {
                boolean pressed = get();

                if (m_pressedLast && !pressed) {
                  command.schedule(interruptible);
                }

                m_pressedLast = pressed;
              }
            });
    return this;
  }

  /**
   * Starts the command when the condition becomes false. The command is set to be interruptible.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onFalse(final Command command) {
    return onFalse(command, true);
  }

  /**
   * Runs the given runnable when the condition becomes false.
   *
   * @param toRun the runnable to run
   * @param requirements the required subsystems
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onFalse(final Runnable toRun, Subsystem... requirements) {
    return onFalse(new InstantCommand(toRun, requirements));
  }

  /**
   * Toggles a command when the condition becomes true.
   *
   * @param command the command to toggle
   * @param interruptible whether the command is interruptible
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent toggleOnTrue(final Command command, boolean interruptible) {
    requireNonNullParam(command, "command", "toggleOnTrue");

    CommandScheduler.getInstance()
        .addButton(
            new Runnable() {
              private boolean m_pressedLast = get();

              @Override
              public void run() {
                boolean pressed = get();

                if (!m_pressedLast && pressed) {
                  if (command.isScheduled()) {
                    command.cancel();
                  } else {
                    command.schedule(interruptible);
                  }
                }

                m_pressedLast = pressed;
              }
            });
    return this;
  }

  /**
   * Toggles a command when the condition becomes true. The command is set to be interruptible.
   *
   * @param command the command to toggle
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent toggleOnTrue(final Command command) {
    return toggleOnTrue(command, true);
  }

  /**
   * Cancels a command when the condition becomes true.
   *
   * @param command the command to cancel
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent cancelOnTrue(final Command command) {
    requireNonNullParam(command, "command", "cancelOnTrue");

    CommandScheduler.getInstance()
        .addButton(
            new Runnable() {
              private boolean m_pressedLast = get();

              @Override
              public void run() {
                boolean pressed = get();

                if (!m_pressedLast && pressed) {
                  command.cancel();
                }

                m_pressedLast = pressed;
              }
            });
    return this;
  }

  /**
   * Composes this BooleanEvent with another BooleanEvent, returning a new BooleanEvent that is true when both
   * BooleanEvents are true.
   *
   * @param eventListener the BooleanEvent to compose with
   * @return the BooleanEvent that is true when both BooleanEvents are true
   */
  public BooleanEvent and(BooleanEvent eventListener) {
    return new BooleanEvent(() -> get() && eventListener.get());
  }

  /**
   * Composes this BooleanEvent with another BooleanEvent, returning a new BooleanEvent that is true when either
   * BooleanEvent is true.
   *
   * @param eventListener the BooleanEvent to compose with
   * @return the BooleanEvent that is true when either BooleanEvent is true
   */
  public BooleanEvent or(BooleanEvent eventListener) {
    return new BooleanEvent(() -> get() || eventListener.get());
  }

  /**
   * Creates a new BooleanEvent that is true when this BooleanEvent is false, i.e. that acts as the
   * negation of this BooleanEvent.
   *
   * @return the negated BooleanEvent
   */
  public BooleanEvent negate() {
    return new BooleanEvent(() -> !get());
  }

  /**
   * Creates a new debounced BooleanEvent from this BooleanEvent - it will become true when this BooleanEvent has
   * been true for longer than the specified period.
   *
   * @param seconds The debounce period.
   * @return The debounced BooleanEvent (rising edges debounced only)
   */
  public BooleanEvent debounce(double seconds) {
    return debounce(seconds, Debouncer.DebounceType.kRising);
  }

  /**
   * Creates a new debounced BooleanEvent from this BooleanEvent - it will become true when this BooleanEvent has
   * been true for longer than the specified period.
   *
   * @param seconds The debounce period.
   * @param type The debounce type.
   * @return The debounced BooleanEvent.
   */
  public BooleanEvent debounce(double seconds, Debouncer.DebounceType type) {
    return new BooleanEvent(
        new BooleanSupplier() {
          Debouncer m_debouncer = new Debouncer(seconds, type);

          @Override
          public boolean getAsBoolean() {
            return m_debouncer.calculate(get());
          }
        });
  }
}
