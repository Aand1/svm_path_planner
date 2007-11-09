package carrito.server.serial;
/*
 * @(#)PortRequestedDialog.java	1.3 98/06/04 SMI
 *
 * Copyright (c) 1998 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license
 * to use, modify and redistribute this software in source and binary
 * code form, provided that i) this copyright notice and license appear
 * on all copies of the software; and ii) Licensee does not utilize the
 * software in a manner which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THE
 * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS
 * BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING
 * OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control
 * of aircraft, air traffic, aircraft navigation or aircraft
 * communications; or in the design, construction, operation or
 * maintenance of any nuclear facility. Licensee represents and
 * warrants that it will not use or redistribute the Software for such
 * purposes.
 */

import java.awt.*;
import java.awt.event.*;
import javax.comm.*;

/**
Informs the user that an other application has requested the port they
are using, and then asks if they are willing to give it up. If the user
answers "Yes" the port is closed and the dialog is closed, if the user
answers "No" the dialog closes and no other action is taken.
*/
public class PortRequestedDialog implements ActionListener {



    /**
    Creates the a dialog with two buttons and a message asking the user if
    they are willing to give up the port they are using.
    */
    public PortRequestedDialog() {


	String lineOne = "Your port has been requested";
	String lineTwo = "by an other application.";
	String lineThree = "Do you want to give up your port?";

    }

    /**
    Handles events generated by the buttons. If the yes button in pushed the
    port closing routine is called and the dialog is disposed of. If the "No"
    button is pushed the dialog is disposed of.
    */
    public void actionPerformed(ActionEvent e) {
	String cmd = e.getActionCommand();





    }
}