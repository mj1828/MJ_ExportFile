package com.mj.exportfile.handlers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.Workbench;
import org.osgi.framework.Bundle;

import com.mj.exportfile.Activator;
import com.mj.exportfile.util.DirList;
import com.mj.exportfile.util.XMLUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OpenHandler extends AbstractHandler {

	public static boolean isNotEmpty(String str) {
		return (str != null && str.length() != 0);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		URL url = bundle.getResource("/prop.properties");
		Properties prop = new Properties();

		try {
			InputStream is = FileLocator.toFileURL(url).openStream();
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();// 获取工作空间
		String exportPath = prop.getProperty("ExportPath");

		ISelectionService selectionService = Workbench.getInstance().getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService.getSelection();

		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element == null) {
				MessageDialog.openWarning(window.getShell(), "Warning",
						"Please select a file or activate the edit window");
				return null;
			} else if (element instanceof IJavaElement) {
				IJavaElement ije = (IJavaElement) element;
				if (isNotEmpty(ije.getPath().getFileExtension()) && ije.getPath().getFileExtension().equals("java")) {
					String projectName = ije.getJavaProject().getElementName(); // 应用名称
					Map<String, String> xml = XMLUtil.xml(workspace, projectName);
					String filepath = ije.getPath().toOSString();
					if (isNotEmpty(filepath) && isNotEmpty(projectName)) {
						if (filepath.indexOf("\\") == 0) {
							filepath = filepath.replaceFirst("\\\\", "");
						}
						String[] str = filepath.split("\\\\");
						if (str.length > 2 && filepath.contains(projectName)) {
							// 替换两遍最大化保证不会出现\\
							filepath = filepath.substring(filepath.indexOf("\\") + 1);
							filepath = filepath.substring(filepath.indexOf("\\"));

							String path = xml.get("output") + filepath.substring(0, filepath.lastIndexOf("\\"));
							String filename = filepath.substring(filepath.lastIndexOf("\\") + 1).replaceAll(".java",
									"");

							if (!new File(exportPath + projectName + "\\" + path + "\\" + filename + ".class")
									.exists()) {
								MessageDialog.openWarning(window.getShell(), "Warning", "File not fount");
							} else {
								try {
									if (System.getProperties().getProperty("os.name").equals("Mac OS X")) {
										Runtime.getRuntime()
												.exec("open -a finder " + exportPath + projectName + "/" + path);
									} else {
										String[] cmd = new String[5];
										cmd[0] = "cmd";
										cmd[1] = "/c";
										cmd[2] = "start";
										cmd[3] = " ";
										cmd[4] = exportPath + projectName + "\\" + path;

										Runtime.getRuntime().exec(cmd);
									}
								} catch (IOException e) {
									e.printStackTrace();
									MessageDialog.openError(window.getShell(), "Warning", "Error");
								}
							}
							return null;
						}
					}

					MessageDialog.openWarning(window.getShell(), "Warning", "Do not support this type of file export");
				} else {
					MessageDialog.openWarning(window.getShell(), "Warning", "Do not support this type of file export");
				}
			} else if (element instanceof IResource) {
				IResource ir = (IResource) element;
				if (isNotEmpty(ir.getFileExtension())
						&& (ir.getFileExtension().equals("zhtml") || ir.getFileExtension().equals("jsp")
								|| ir.getFileExtension().equals("js") || ir.getFileExtension().equals("css"))) {
					String newfilepath = exportPath + ir.getFullPath().toOSString();
					if (System.getProperties().getProperty("os.name").equals("Mac OS X")) {
						newfilepath = newfilepath.replaceAll("//", "/");
					}
					if (!new File(newfilepath).exists()) {
						MessageDialog.openWarning(window.getShell(), "Warning", "File not fount");
					} else {
						String str = ir.getFullPath().toOSString();

						try {
							if (System.getProperties().getProperty("os.name").equals("Mac OS X")) {
								Runtime.getRuntime().exec(
										"open -a finder " + exportPath + str.substring(0, str.lastIndexOf("/") + 1));
							} else {
								String dir = exportPath + str.substring(0, str.lastIndexOf("\\") + 1);
								String[] cmd = new String[5];
								cmd[0] = "cmd";
								cmd[1] = "/c";
								cmd[2] = "start";
								cmd[3] = " ";
								cmd[4] = dir;
								Runtime.getRuntime().exec(cmd);
							}

						} catch (IOException e) {
							MessageDialog.openError(window.getShell(), "Warning", "Error");
						}
					}
				} else {
					MessageDialog.openInformation(window.getShell(), "Success",
							"Do not support this type of file export");
				}
			} else if (element instanceof PackageFragmentRootContainer) {
				MessageDialog.openInformation(window.getShell(), "Success", "Do not support this type of file export");
				PackageFragmentRootContainer pprc = (PackageFragmentRootContainer) element;
				System.out.println(4);
				System.out.println(pprc.getLabel());
				System.out.println(pprc.toString());
			} else {
				MessageDialog.openWarning(window.getShell(), "Warning", "Do not support this type of file export");
			}
		} else {
			// 取得工作台窗体，并取得窗体内处于活动状态的编辑框
			IWorkbenchPage page = window.getActivePage();
			IEditorPart part = page.getActiveEditor();

			String fileName = part.getTitleToolTip();
			if (isNotEmpty(fileName) && fileName.endsWith(".java")) {
				String ext = "\\";
				if (fileName.indexOf(ext) == -1) {
					ext = "/";
				}
				String projectName = fileName.substring(0, fileName.indexOf(ext));
				String[] str = fileName.split(ext);

				Map<String, String> xml = XMLUtil.xml(workspace, projectName);

				if (str.length > 2 && fileName.contains(projectName)) {
					// 继续替换两边防止内部类
					fileName = fileName.substring(fileName.indexOf(ext) + 1);
					fileName = fileName.substring(fileName.indexOf(ext));

					String path = projectName + ext + xml.get("output")
							+ fileName.substring(0, fileName.lastIndexOf(ext));
					String dir = workspace + ext + path;
					String fname = fileName.substring(fileName.lastIndexOf(ext) + 1).replaceAll(".java", "");

					String[] files = DirList.getFile(dir, fname + "(\\$)?([0-9])*\\.class");
					if (files == null || files.length == 0) {
						MessageDialog.openWarning(window.getShell(), "Warning",
								"Please wait a few seconds and try again");
						return null;
					}
					if (!new File(exportPath + path + ext + fname + ".class").exists()) {
						MessageDialog.openWarning(window.getShell(), "Warning", "The current editor file not found("
								+ exportPath + path + ext + fname + ".class" + ")");
					} else {
						try {

							if (System.getProperties().getProperty("os.name").equals("Mac OS X")) {
								Runtime.getRuntime().exec("open -a finder " + exportPath + path + ext);
							} else {
								String[] cmd = new String[5];
								cmd[0] = "cmd";
								cmd[1] = "/c";
								cmd[2] = "start";
								cmd[3] = " ";
								cmd[4] = exportPath + path + ext;
								Runtime.getRuntime().exec(cmd);
							}
						} catch (IOException e) {
							MessageDialog.openError(window.getShell(), "Warning", "Error");
						}
					}
					return null;
				}
			} else if (isNotEmpty(exportPath + fileName)) {
				if (!new File(exportPath + fileName).exists()) {
					MessageDialog.openWarning(window.getShell(), "Warning",
							"The current editor file not found(" + exportPath + fileName + ")");
				} else {
					try {
						if (System.getProperties().getProperty("os.name").equals("Mac OS X")) {
							Runtime.getRuntime().exec("open -a finder " + exportPath
									+ fileName.substring(0, fileName.lastIndexOf("/") + 1));
						} else {
							String dir = exportPath + fileName.substring(0, fileName.lastIndexOf("\\") + 1);
							String[] cmd = new String[5];
							cmd[0] = "cmd";
							cmd[1] = "/c";
							cmd[2] = "start";
							cmd[3] = " ";
							cmd[4] = dir;
							Runtime.getRuntime().exec(cmd);
						}

					} catch (IOException e) {
						MessageDialog.openError(window.getShell(), "Warning", "Error");
					}
				}
			} else {
				MessageDialog.openWarning(window.getShell(), "Warning",
						"Please select a file or activate the edit window");
			}
		}
		return null;
	}
}
