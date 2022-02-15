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
import java.util.function.Consumer;

/**
 * This class provides an easy way to link commands to boolean inputs such as joystick buttons, limit switches, or robot status.
 *
 * <p>It is very easy to link an event to a command. For instance, you could link the BooleanEvent button
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
   * Returns whether or not the BooleanEvent is true.
   *
   * <p>This method will be called repeatedly when a command is linked to the BooleanEvent.
   *
   * <p>Functionally identical to {@link BooleanEvent#getAsBoolean()}.
   *
   * @return whether or not the BooleanEvent is true.
   */
  public boolean get() {
    return this.getAsBoolean();
  }

/**
   * Returns whether or not the BooleanEvent is true.
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

  /* BINDING TO TRUE */

  /**
   * Starts the given command whenever the BooleanEvent becomes true. 
   * 
   * <p>The command is set to be interruptible, and will not be restarted if it ends.
   *
   * @param command the command to start
   * @param interruptOnFalse whether the command should be interrupted when the BooleanEvent later becomes false
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onTrue(final Command command, boolean interruptOnFalse) {
    requireNonNullParam(command, "command", "onTrue");
    onChange(state -> {
      if(state == true) {
        command.schedule();
      } else if (interruptOnFalse) {
        command.cancel();
      }
    });
    return this;
  }

  /**
   * Starts the given command whenever the BooleanEvent becomes true. 
   * 
   * <p>The command is set to be interruptible, and will not be restarted if it ends.
   * It will also not be interrupted when the BooleanEvent later becomes false.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onTrue(final Command command) {
    return onTrue(command, false);
  }

  /**
   * Runs the given Runnable whenever the BooleanEvent becomes true.
   * 
   * <p>This method schedules an InstantCommand with the given requirements to run the Runnable.
   *
   * @param toRun the Runnable to run
   * @param requirements the required subsystems
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onTrue(final Runnable toRun, Subsystem... requirements) {
    return onTrue(new InstantCommand(toRun, requirements), false);
  }

  /**
   * Runs the given Runnable whenever the BooleanEvent just becomes true.
   * 
   * <p>This method does not schedule any commands nor deal with requirements.
   *
   * @param toRun the Runnable to run
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onTrue(final Runnable toRun) {
     onChange(state -> {
       if (state == true) {
         toRun.run();
       }
     });
     return this;
   }

  /**
   * Constantly starts the given command while the BooleanEvent is true.
   *
   * <p>{@link Command#schedule(boolean)} will be called repeatedly while the BooleanEvent is true.
   * If `interruptOnFalse` is `true`, the command will be canceled when the BooleanEvent becomes false. 
   * 
   * <p>Note that scheduling a command before it ends does nothing, so this method restarts commands as soon as they end, but not before.
   *
   * @param command the command to start
   * @param interruptOnFalse whether the command should be interrupted when the BooleanEvent later becomes false
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrue(final Command command, boolean interruptOnFalse) {
    requireNonNullParam(command, "command", "whileTrue");
    whileTrue(()->{command.schedule();});
    if(interruptOnFalse) {
      onChange(state -> {
        if (state == false) {
          command.cancel();
        }
      });
    }
    return this;
  }

  /**
   * Constantly starts the given command while the BooleanEvent is true.
   *
   * <p>{@link Command#schedule(boolean)} will be called repeatedly while the BooleanEvent is true, 
   * but the command will not be canceled when the BooleanEvent becomes false.
   * 
   * <p>Note that scheduling a command before it ends does nothing, so this method restarts commands as soon as they end, but not before.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrue(final Command command) {
    return whileTrue(command, true);
  }

  /**
   * Constantly runs the given Runnable while the BooleanEvent is true.
   * 
   * <p>This method constantly schedules an InstantCommand with the given requirements to run the Runnable.
   *
   * @param toRun the Runnable to run
   * @param requirements the required subsystems
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrue(final Runnable toRun, Subsystem... requirements) {
    requireNonNullParam(toRun, "command", "whileTrue");
    return whileTrue(new InstantCommand(toRun, requirements));
  }

  /**
   * Constantly runs the given Runnable while the BooleanEvent is true.
   * 
   * <p>This method does not schedule any commands or deal with requirements. 
   * @param toRun the Runnable to run
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrue(final Runnable toRun) {
    requireNonNullParam(toRun, "toRun", "whileTrue");
    CommandScheduler.getInstance().addButton(
      () -> {
        if (get() == true) {
          toRun.run();
        }
      }
    );
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
  public BooleanEvent toggleOnTrue(final Command command) {
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
  public BooleanEvent cancelOnTrue(final Command command) {
    requireNonNullParam(command, "command", "cancelOnTrue");
    onTrue(
      ()-> {
        command.cancel();
      }
    );
    return this;
  }

  /* BINDING TO FALSE */

  /**
   * Starts the given command whenever the BooleanEvent becomes false. 
   * 
   * <p>The command is set to be interruptible, and will not be restarted if it ends.
   *
   * @param command the command to start
   * @param interruptOnFalse whether the command should be interrupted when the BooleanEvent later becomes true
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onFalse(final Command command, boolean interruptOnTrue) {
    requireNonNullParam(command, "command", "onFalse");
    onChange(state -> {
      if(state == false) {
        command.schedule();
      } else if (interruptOnTrue) {
        command.cancel();
      }
    });
    return this;
  }

/**
   * Starts the given command whenever the BooleanEvent becomes true. 
   * 
   * <p>The command is set to be interruptible, and will not be restarted if it ends.
   * It will also not be interrupted when the BooleanEvent later becomes true.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onFalse(final Command command) {
    return onFalse(command, false);
  }

  /**
   * Runs the given Runnable whenever the BooleanEvent becomes false.
   * 
   * <p>This method schedules an InstantCommand with the given requirements to run the Runnable.
   *
   * @param toRun the Runnable to run
   * @param requirements the required subsystems
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onFalse(final Runnable toRun, Subsystem... requirements) {
    return onFalse(new InstantCommand(toRun, requirements), false);
  }

  /**
   * Runs the given Runnable whenever the BooleanEvent just becomes false.
   * 
   * <p>This method does not schedule any commands nor deal with requirements.
   *
   * @param toRun the Runnable to run
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onFalse(final Runnable toRun) {
     onChange(state -> {
       if (state == false) {
         toRun.run();
       }
     });
     return this;
   }

  /**
   * Constantly starts the given command while the BooleanEvent is false.
   *
   * <p>{@link Command#schedule(boolean)} will be called repeatedly while the BooleanEvent is false.
   * If `interruptOnFalse` is `true`, the command will be canceled when the BooleanEvent becomes true. 
   * 
   * <p>Note that scheduling a command before it ends does nothing, so this method restarts commands as soon as they end, but not before.
   *
   * @param command the command to start
   * @param interruptOnFalse whether the command should be interrupted when the BooleanEvent later becomes false
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileFalse(final Command command, boolean interruptOnTrue) {
    requireNonNullParam(command, "command", "whileFalse");
    whileFalse(()->{command.schedule();});
    if(interruptOnTrue) {
      onChange(state -> {
        if (state == true) {
          command.cancel();
        }
      });
    }
    return this;
  }

  /**
   * Constantly starts the given command while the BooleanEvent is false.
   *
   * <p>{@link Command#schedule(boolean)} will be called repeatedly while the BooleanEvent is false, 
   * but the command will not be canceled when the BooleanEvent becomes true.
   * 
   * <p>Note that scheduling a command before it ends does nothing, so this method restarts commands as soon as they end, but not before.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileFalse(final Command command) {
    return whileFalse(command, true);
  }

  /**
   * Constantly runs the given Runnable while the BooleanEvent is false.
   * 
   * <p>This method constantly schedules an InstantCommand with the given requirements to run the Runnable.
   *
   * @param toRun the Runnable to run
   * @param requirements the required subsystems
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileFalse(final Runnable toRun, Subsystem... requirements) {
    requireNonNullParam(toRun, "command", "whileFalse");
    return whileFalse(new InstantCommand(toRun, requirements));
  }

  /**
   * Constantly runs the given Runnable while the BooleanEvent is false.
   * 
   * <p>This method does not schedule any commands or deal with requirements. 
   * @param toRun the Runnable to run
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileFalse(final Runnable toRun) {
    requireNonNullParam(toRun, "toRun", "whileFalse");
    CommandScheduler.getInstance().addButton(
      () -> {
        if (get() == false) {
          toRun.run();
        }
      }
    );
    return this;
  }
  
  /**
   * Toggles a command when the BooleanEvent becomes false.
   * 
   * <p>This method will cancel the command if it is running, or schedule it if it is not running.
   *
   * @param command the command to toggle
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent toggleOnFalse(final Command command) {
    requireNonNullParam(command, "command", "toggleOnFalse");
    onFalse(
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
   * Cancels a command when the BooleanEvent becomes false.
   *
   * @param command the command to cancel
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent cancelOnFalse(final Command command) {
    requireNonNullParam(command, "command", "cancelOnFalse");
    onFalse(
      ()-> {
        command.cancel();
      }
    );
    return this;
  }


  /**
   * Runs the given Consumer when this BooleanEvent changes state between true and false.
   * 
   * @param handler the Consumer to run, which accepts the new state
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onChange(Consumer<Boolean> handler) {
    requireNonNullParam(handler, "handle", "onChange");
    CommandScheduler.getInstance()
        .addButton(
            new Runnable() {
              private boolean m_stateLast = get();

              @Override
              public void run() {
                boolean state = get();

                if (m_stateLast != state) {
                  handler.accept(state);
                }

                m_stateLast = state;
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
