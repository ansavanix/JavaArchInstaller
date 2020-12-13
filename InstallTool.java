import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

public class InstallTool
{
	private Scanner textScanner;
	
	public InstallTool()
	{
		textScanner = new Scanner(System.in);
		
	}
	public String ask(String prompt, String options)
	{
		boolean correct = false;
		String response = "null";
		while(!correct)
		{
			System.out.println("Enter " + prompt + " " + options);
			response = textScanner.nextLine();
			correct = true;
			//correct = confirmation(prompt);
		}
		return response;
	}
	public boolean confirmation(String prompt)
	{
		String confirmation;
		boolean confirmed = false;
		int x = 0;
		while (x != 1)
		{
			
			System.out.println("Are you sure this is the correct " + prompt +"? y/n");
			confirmation = textScanner.nextLine();
			if (confirmation.equals("y"))
			{
				x = 1;
				confirmed = true;
			} 
			else if (confirmation.equals("n"))
			{
				x = 1;
			}
			else
			{
				System.out.println("INVALID INPUT! y/n");
			}
			
		}
		return confirmed;
	}
	public String createInstallString(String theRootDisk,String theBootMethod,String theKernel,String theFirmware,String theHostname, String theMicrocode)
	{
		String tabletype;
		if (theBootMethod.equals("uefi"))
		{
			tabletype = "gpt";
		} 
		else
		{
			tabletype = "msdos";
		}
		String firstPartition;
		if (tabletype.equals("gpt"))
		{
			firstPartition = "fat32 1 512";
		}
		else
		{
			firstPartition = "ext4 1 100%FREE";
		}
		String installString = "timedatectl set-ntp true\n";
		installString = installString + "parted /dev/" + theRootDisk + " mklabel " + tabletype + "\n";
		installString = installString + "parted /dev/" + theRootDisk + " mkpart " + firstPartition + "\n";
		if (theBootMethod.equals("uefi"))
		{
			installString = installString + "parted /dev/" + theRootDisk + " mkpart ext4 512 100%FREE" + "\n";
			installString = installString + "mount /dev/"+ theRootDisk+"2 /mnt\n";
			installString = installString + "mkdir /mnt/boot\n";
			installString = installString + "mkdir /mnt/boot/efi\n";
			installString = installString + "mount /dev/" + theRootDisk + "1 /mnt/boot/efi\n";
		}
		else
		{
			installString = installString + "mount /dev/" + theRootDisk + "1 /mnt\n";


		}
		String kernelString;
		if (theKernel.equals("lts"))
		{
			kernelString = "linux-lts";
		}
		else if (theKernel.equals("zen")){
			kernelString = "linux-zen";
		}
		else if (theKernel.equals("hardened"))
		{
			kernelString = "linux-hardened";
		}
		else
		{
			kernelString = "linux";
		}
		String firmwareString;
		if (theFirmware.equals("n"))
		{
			firmwareString = "";
		}
		else
		{
			firmwareString = " linux-firmware";
		}
		installString = installString + "pacstrap /mnt base " + kernelString +firmwareString + "\n";
		installString = installString + "genfstab -U /mnt >> /mnt/etc/fstab\n";
		installString = installString + "arch-chroot /mnt timedatectl set-timezone America/Los_Angeles\n";
		installString = installString + "arch-chroot /mnt echo en_US.UTF-8 UTF-8 > /etc/locale.gen\n";
		installString = installString + "arch-chroot /mnt locale-gen\n";
		installString = installString + "arch-chroot /mnt echo LANG=en_US.UTF-8 > /etc/locale.conf\n";
		installString = installString + "arch-chroot /mnt echo " + theHostname + " > /etc/hostname\n";
		installString = installString + "arch-chroot /mnt passwd\n";
		if (theMicrocode.equals("y"))
		{
			installString = installString + "arch-chroot /mnt pacman -S intel-ucode amd-ucode\n";
		}
		if (theBootMethod.equals("uefi"))
		{
			installString = installString + "arch-chroot /mnt pacman -S grub efibootmgr\n";
			installString = installString + "arch-chroot /mnt grub-install --target=x86_64-efi --bootloader-id=GRUB --efi-directory=/boot/efi\n";

		}
		else
		{
			installString = installString + "arch-chroot /mnt pacman -S grub\n";
			installString = installString + "arch-chroot /mnt grub-install /dev/" + theRootDisk+"\n";

		}
		installString = installString + "arch-chroot /mnt grub-mkconfig -o /boot/grub/grub.cfg\n";
		installString = installString + "arch-chroot /mnt pacman -S networkmanager\n";
		installString = installString + "arch-chroot /mnt systemctl enable NetworkManager\n";
		installString = installString + "echo Installation Finished!\n";
		

		return installString;
	}
	public void writeInstallScript(String theInstallScript)
	{
		
		try
		{
			FileWriter scriptWriter = new FileWriter("ArchInstallScript.sh");
			scriptWriter.write(theInstallScript);
			scriptWriter.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args)
	{
		System.out.println("Starting installation process...");
		InstallTool t1 = new InstallTool();
		String rootDisk = t1.ask("root disk","Example: sda");
		String bootMethod = t1.ask("boot method","uefi/bios");
		String kernel = t1.ask("kernel","regular/lts/hardened/zen");
		String firmware = t1.ask("if proprietary firmware is needed","y/n");
		String hostname = t1.ask("hostname/computer name","Example: ArchComputer");
		String microcode = t1.ask("microcode updates","y/n");
		System.out.println("Creating installation shell script...");
		String script = t1.createInstallString(rootDisk,bootMethod,kernel,firmware,hostname,microcode);
		t1.writeInstallScript(script);
		
	}
}


