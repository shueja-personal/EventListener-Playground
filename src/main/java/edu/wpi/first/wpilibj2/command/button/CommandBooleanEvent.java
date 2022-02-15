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

import java.util.Collection;
import java.util.function.BooleanSupplier;

/**
 * This class provides an easy way to link commands to boolean inputs such as joystick buttons, limit switches, or robot status.
 *
 * <p>It is very easy to link an event to a command. For instance, you could link the BooleanEvent button
 * of a joystick to a "score" command.
 *
 * <p>It is encouraged that teams write a subclass of BooleanEvent if they want to have something unusual
 * (for instance, if they want to react to the user holding a button while the robot is reading a
 * certain sensor input). For this, they only have to write the {@link CommandBooleanEvent#get()} method to get
 * the full functionality of the BooleanEvent class.
 *
 * <p>This class is provided by the NewCommands VendorDep
 */
public class CommandBooleanEvent extends BooleanEvent {

  /**
   * Creates a new BooleanEvent that monitors the given condition.
   *
   * @param eventSupplier the condition the BooleanEvent should monitor.
   */
  public CommandBooleanEvent(BooleanSupplier eventSupplier) {
    super(eventSupplier);
    CommandScheduler.getInstance().addButton(this::poll);
  }

  /**
   * Creates a new BooleanEvent that is always false. Useful only as a no-arg constructor for
   * subclasses that will be overriding {@link CommandBooleanEvent#get()} anyway.
   */
  public CommandBooleanEvent() {
    super();
  }

  protected CommandBooleanEvent(BooleanSupplier eventSupplier, Collection<Runnable> handlers) {
    super(eventSupplier, handlers);
  }

  /**
   * Constantly runs the given Runnable while the BooleanEvent is true.
   * 
   * <p>This method schedules an InstantCommand to run the runnable.
   * @param toRun the Runnable to run while the BooleanEvent is true.
   * @return this BooleanEvent, so calls can be chained
   */
  public CommandBooleanEvent whileTrueContinuous(final Runnable toRun, Subsystem... requirements) {
    whileTrueContinuous(new InstantCommand(toRun, requirements));
    return this;
  }

  @Override
  public CommandBooleanEvent whileTrueContinuous(final Runnable toRun) {
    whileTrueContinuous(toRun, new Subsystem[0]);
    return this;
  }

  /**
   * Starts the given command whenever the BooleanEvent becomes true. 
   * 
   * <p>The command is set to be interruptible, and will not be restarted if it ends.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public CommandBooleanEvent onTrue(final Command command) {
    requireNonNullParam(command, "command", "onTrue");
    onTrue(()->{
      if (!command.isScheduled()) {
        command.schedule();
      }
    });
    return this;
  }

  /**
   * Runs the given command only while the BooleanEvent is true, restarting it if it ends.
   *
   * <p>{@link Command#schedule(boolean)} will be called to restart the command.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public CommandBooleanEvent whileTrueContinuous(final Command command) {
    requireNonNullParam(command, "command", "whileTrueContinuous");
    addHandler(
      new Runnable() {
        private boolean m_stateLast = get();

        @Override
        public void run() {
          boolean state = get();

          if (state == true) {
            if (!command.isScheduled()) {
              command.schedule();
            }
          }
          if (m_stateLast == true && state == false) {
            if (command.isScheduled()) {
              command.cancel();
            }
          }

          m_stateLast = state;
        }
      });
    return this;
  }

    /**
   * Starts the given command when the BooleanEvent becomes true, and cancels it when the BooleanEvent becomes false. 
   * 
   * <p>The command will not be restarted if it ends.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public CommandBooleanEvent whileTrueOnce(final Command command) {
    requireNonNullParam(command, "command", "whileTrueOnce");
    onChange(
      state -> {
        if(state == true){
          if (!command.isScheduled()) {
            command.schedule();
          }
        }
        else {
          if (command.isScheduled()) {
            command.cancel();
          }
        }
      });
    return this;
  }
  
  /**
   * Toggles a command when the BooleanEvent becomes true.
   * 
   * <p>This method will cancel the command if it is running, or schedule it if it is not running.
   *
   * @param command the command to toggle
   * @return this BooleanEvent, so calls can be chained
   */
  public CommandBooleanEvent toggleOnTrue(final Command command) {
    requireNonNullParam(command, "command", "toggleOnTrue");
    onTrue(
      ()->{
        if (command.isScheduled()) {
          command.cancel();
        } else {
          command.schedule();
        }
      }
    );
    return this;
  }

  /**
   * Cancels a command when the BooleanEvent becomes true.
   *
   * @param command the command to cancel
   * @return this BooleanEvent, so calls can be chained
   */
  public CommandBooleanEvent cancelOnTrue(final Command command) {
    requireNonNullParam(command, "command", "cancelOnTrue");
    onTrue(
      ()-> {
        if (command.isScheduled()) {
          command.cancel();
        }
      }
    );
    return this;
  }

  /* COMPOSITION */
  /**
   * Composes this BooleanEvent with another BooleanEvent, returning a new BooleanEvent that is true when both
   * BooleanEvents are true.
   *
   * @param eventListener the BooleanEvent to compose with
   * @return the BooleanEvent that is true when both BooleanEvents are true
   */
  public CommandBooleanEvent and(BooleanEvent eventListener) {
    Collection<Runnable> handlers = m_handlers;
    handlers.addAll(eventListener.m_handlers);
    return new CommandBooleanEvent(
      () -> get() && eventListener.get(),
      handlers);
  }
  /**
   * Composes this BooleanEvent with another BooleanEvent, returning a new BooleanEvent that is true when either
   * BooleanEvent is true.
   *
   * @param eventListener the BooleanEvent to compose with
   * @return the BooleanEvent that is true when either BooleanEvent is true
   */
  public CommandBooleanEvent or(BooleanEvent eventListener) {
    Collection<Runnable> handlers = m_handlers;
    handlers.addAll(eventListener.m_handlers);
    return new CommandBooleanEvent(
      () -> get() || eventListener.get(),
      handlers);
  }

  /**
   * Creates a new BooleanEvent that is true when this BooleanEvent is false, i.e. that acts as the
   * negation of this BooleanEvent.
   * 
   * <p>IMPORTANT: the bindings from the BooleanEvent being negated will not be copied,
   * to avoid actions being bound to the negation of their intended condition
   *
   * @return the negated BooleanEvent
   */
  public CommandBooleanEvent negate() {
    return new CommandBooleanEvent(() -> !get());
  }

  /**
   * Creates a new debounced BooleanEvent from this BooleanEvent - it will become true when this BooleanEvent has
   * been true for longer than the specified period.
   *
   * @param seconds The debounce period.
   * @return The debounced BooleanEvent (rising edges debounced only)
   */
  public CommandBooleanEvent debounce(double seconds) {
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
  public CommandBooleanEvent debounce(double seconds, Debouncer.DebounceType type) {
    return new CommandBooleanEvent(
        new BooleanSupplier() {
          Debouncer m_debouncer = new Debouncer(seconds, type);

          @Override
          public boolean getAsBoolean() {
            return m_debouncer.calculate(get());
          }
        },
        m_handlers);
  }
}
