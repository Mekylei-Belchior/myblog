import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
    providedIn: 'root'
})
export class DebugUtil {
    private get isDebugMode(): boolean {
        // Enables debug if not in production or if debug is forced in localStorage
        return !environment.production || localStorage.getItem('forceDebug') === 'true';
    }

    /**
     * Logs error messages only in debug mode
     * @param message Message or error to be logged
     * @param context Context where the error occurred (optional)
     * @param extras Extra information (optional)
     */
    error(message: any, context?: string, extras?: any): void {
        if (!this.isDebugMode) return;

        console.error(`[ERROR]${context ? ` [${context}]` : ''}`, message);
        if (extras) {
            console.error('Extras:', extras);
        }

        // If it's an Error, show the stack trace
        if (message instanceof Error && message.stack) {
            console.error(message.stack);
        }
    }

    /**
     * Logs warning messages only in debug mode
     * @param message Message to be logged
     * @param context Context (optional)
     */
    warn(message: any, context?: string): void {
        if (!this.isDebugMode) return;
        console.warn(`[WARN]${context ? ` [${context}]` : ''}`, message);
    }

    /**
     * Logs informational messages only in debug mode
     * @param message Message to be logged
     * @param context Context (optional)
     */
    info(message: any, context?: string): void {
        if (!this.isDebugMode) return;
        console.info(`[INFO]${context ? ` [${context}]` : ''}`, message);
    }

    /**
     * Logs debug messages (most detailed level)
     * @param message Message to be logged
     * @param context Context (optional)
     */
    debug(message: any, context?: string): void {
        if (!this.isDebugMode) return;
        console.log(`[DEBUG]${context ? ` [${context}]` : ''}`, message);
    }

    /**
     * Logs messages conditionally
     * @param condition Condition to log the message
     * @param message Message to be logged
     * @param context Context (optional)
     */
    assert(condition: boolean, message: string, context?: string): void {
        if (!this.isDebugMode) return;
        if (!condition) {
            console.error(`[ASSERT FAILED]${context ? ` [${context}]` : ''}`, message);
        }
    }

    /**
     * Starts a log group
     * @param label Group label
     * @param collapsed Whether the group should start collapsed
     */
    group(label: string, collapsed: boolean = false): void {
        if (!this.isDebugMode) return;
        if (collapsed) {
            console.groupCollapsed(label);
        } else {
            console.group(label);
        }
    }

    /**
     * Ends a log group
     */
    groupEnd(): void {
        if (!this.isDebugMode) return;
        console.groupEnd();
    }
}