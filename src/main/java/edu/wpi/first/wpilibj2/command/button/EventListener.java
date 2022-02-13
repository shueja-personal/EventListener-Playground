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
 * <p>It is very easy to link a button to a command. For instance, you could link the EventListener button
 * of a joystick to a "score" command.
 *
 * <p>It is encouraged that teams write a subclass of EventListener if they want to have something unusual
 * (for instance, if they want to react to the user holding a button while the robot is reading a
 * certain sensor input). For this, they only have to write the {@link EventListener#get()} method to get
 * the full functionality of the EventListener class.
 *
 * <p>This class is provided by the NewCommands VendorDep
 */
public class EventListener implements BooleanSupplier {
  private final BooleanSupplier m_condition;

  /**
   * Creates a new EventListener that monitors the given condition.
   *
   * @param condition the condition the EventListener should monitor.
   */
  public EventListener(BooleanSupplier condition) {
    m_condition = condition;
  }

  /**
   * Creates a new EventListener that is always false. Useful only as a no-arg constructor for
   * subclasses that will be overriding {@link EventListener#get()} anyway.
   */
  public EventListener() {
    m_condition = () -> false;
  }

  /**
   * Returns whether or not the EventListener's condition is true.
   *
   * <p>This method will be called repeatedly when a command is linked to the EventListener.
   *
   * <p>Functionally identical to {@link EventListener#getAsBoolean()}.
   *
   * @return whether or not the EventListener condition is true.
   */
  public boolean get() {
    return this.getAsBoolean();
  }

/**
   * Returns whether or not the EventListener's condition is true.
   *
   * <p>This method will be called repeatedly when a command is linked to the EventListener.
   *
   * <p>Functionally identical to {@link EventListener#get()}.
   *
   * @return whether or not the EventListener condition is true.
   */
  @Override
  public boolean getAsBoolean() {
    return m_condition.getAsBoolean();
  }

  /**
   * Starts the given command whenever the EventListener's condition just becomes true.
   *
   * @param command the command to start
   * @param interruptible whether the command is interruptible
   * @return this EventListener, so calls can be chained
   */
  public EventListener onTrue(final Command command, boolean interruptible) {
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
   * Starts the given command whenever the EventListener's condition just becomes true. The command is set to be
   * interruptible.
   *
   * @param command the command to start
   * @return this EventListener, so calls can be chained
   */
  public EventListener onTrue(final Command command) {
    return onTrue(command, true);
  }

  /**
   * Runs the given runnable whenever the EventListener's condition just becomes true.
   *
   * @param toRun the runnable to run
   * @param requirements the required subsystems
   * @return this EventListener, so calls can be chained
   */
  public EventListener onTrue(final Runnable toRun, Subsystem... requirements) {
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
   * @return this EventListener, so calls can be chained
   */
  public EventListener whileTrueContinuous(final Command command, boolean interruptible) {
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
   * @return this EventListener, so calls can be chained
   */
  public EventListener whileTrueContinuous(final Command command) {
    return whileTrueContinuous(command, true);
  }

  /**
   * Constantly runs the given runnable while the condition is true.
   *
   * @param toRun the runnable to run
   * @param requirements the required subsystems
   * @return this EventListener, so calls can be chained
   */
  public EventListener whileTrueContinuous(final Runnable toRun, Subsystem... requirements) {
    return whileTrueContinuous(new InstantCommand(toRun, requirements));
  }

  /**
   * Starts the given command when the condition initially becomes true, and ends it when it becomes
   * false, but does not re-start it in-between.
   *
   * @param command the command to start
   * @param interruptible whether the command is interruptible
   * @return this EventListener, so calls can be chained
   */
  public EventListener whileTrueOnce(final Command command, boolean interruptible) {
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
   * @return this EventListener, so calls can be chained
   */
  public EventListener whileTrueOnce(final Command command) {
    return whileTrueOnce(command, true);
  }

  /**
   * Starts the command when the condition becomes false.
   *
   * @param command the command to start
   * @param interruptible whether the command is interruptible
   * @return this EventListener, so calls can be chained
   */
  public EventListener onFalse(final Command command, boolean interruptible) {
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
   * @return this EventListener, so calls can be chained
   */
  public EventListener onFalse(final Command command) {
    return onFalse(command, true);
  }

  /**
   * Runs the given runnable when the condition becomes false.
   *
   * @param toRun the runnable to run
   * @param requirements the required subsystems
   * @return this EventListener, so calls can be chained
   */
  public EventListener onFalse(final Runnable toRun, Subsystem... requirements) {
    return onFalse(new InstantCommand(toRun, requirements));
  }

  /**
   * Toggles a command when the condition becomes true.
   *
   * @param command the command to toggle
   * @param interruptible whether the command is interruptible
   * @return this EventListener, so calls can be chained
   */
  public EventListener toggleOnTrue(final Command command, boolean interruptible) {
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
   * @return this EventListener, so calls can be chained
   */
  public EventListener toggleOnTrue(final Command command) {
    return toggleOnTrue(command, true);
  }

  /**
   * Cancels a command when the condition becomes true.
   *
   * @param command the command to cancel
   * @return this EventListener, so calls can be chained
   */
  public EventListener cancelOnTrue(final Command command) {
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
   * Composes this EventListener with another EventListener, returning a new EventListener that is true when both
   * EventListeners are true.
   *
   * @param eventListener the EventListener to compose with
   * @return the EventListener that is true when both EventListeners are true
   */
  public EventListener and(EventListener eventListener) {
    return new EventListener(() -> get() && eventListener.get());
  }

  /**
   * Composes this EventListener with another EventListener, returning a new EventListener that is true when either
   * EventListener is true.
   *
   * @param eventListener the EventListener to compose with
   * @return the EventListener that is true when either EventListener is true
   */
  public EventListener or(EventListener eventListener) {
    return new EventListener(() -> get() || eventListener.get());
  }

  /**
   * Creates a new EventListener that is true when this EventListener is false, i.e. that acts as the
   * negation of this EventListener.
   *
   * @return the negated EventListener
   */
  public EventListener negate() {
    return new EventListener(() -> !get());
  }

  /**
   * Creates a new debounced EventListener from this EventListener - it will become true when this EventListener has
   * been true for longer than the specified period.
   *
   * @param seconds The debounce period.
   * @return The debounced EventListener (rising edges debounced only)
   */
  public EventListener debounce(double seconds) {
    return debounce(seconds, Debouncer.DebounceType.kRising);
  }

  /**
   * Creates a new debounced EventListener from this EventListener - it will become true when this EventListener has
   * been true for longer than the specified period.
   *
   * @param seconds The debounce period.
   * @param type The debounce type.
   * @return The debounced EventListener.
   */
  public EventListener debounce(double seconds, Debouncer.DebounceType type) {
    return new EventListener(
        new BooleanSupplier() {
          Debouncer m_debouncer = new Debouncer(seconds, type);

          @Override
          public boolean getAsBoolean() {
            return m_debouncer.calculate(get());
          }
        });
  }
}
