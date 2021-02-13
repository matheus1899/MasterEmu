package uk.co.philpotter.masteremu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.zip.CRC32;

public class FileBrowserAdapter extends RecyclerView.Adapter<FileBrowserAdapter.ItemViewHolder> {
    private FileBrowserActivity activity;
    private Object[] filesAndFolders;

    public FileBrowserAdapter(FileBrowserActivity activity, Object[] filesAndFolders){
        this.activity = activity;
        this.filesAndFolders = filesAndFolders;
    }
    @NonNull @Override public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.file_browser_item_list, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(position);
    }
    @Override public int getItemCount() {
        return filesAndFolders.length;
    }
    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView txtView;
        ImageView imgView;
        View root;
        int position;
        ItemViewHolder(View view){
            super(view);
            root = view;
            txtView = view.findViewById(R.id.file_browser_item_list_txt);
            imgView = view.findViewById(R.id.file_browser_item_list_img);
        }
        public void bind(int position){
            this.position = position;
            this.root.setOnClickListener(this);
            File f = (File)filesAndFolders[position];

            if (f.getPath().equals(activity.defaultFilePath)) {
                imgView.setImageResource(R.drawable.ic_set_default_folder);
            }
            else if (f.isDirectory()) {
                imgView.setImageResource(R.drawable.ic_folder);
            }
            else {
                imgView.setImageResource(R.drawable.ic_file);
            }

            if (f.getName().endsWith("..") || ((EmuFile)filesAndFolders[position]).isParent()) {
                imgView.setImageResource(R.drawable.ic_arrow_back);
                txtView.setText("..");
            }
            else if (f.getPath().equals(activity.defaultFilePath) && activity.actionType.equals("load_rom")) {
                txtView.setText("SET DEFAULT FOLDER");
            }
            else if (f.getPath().equals(activity.thisDirPath) && activity.actionType.equals("export_states")) {
                txtView.setText("Export Here");
            }
            else {
                //String name = f.getName();
                //if (name.length() > 40)
                //    name = name.substring(0, 40 - 3) + "...";
                txtView.setText(f.getName());
            }

        }

        @Override public void onClick(View view) {
            File file = (File)filesAndFolders[position];
            String filePath = file.getPath().toLowerCase();
            if (activity.actionType.equals("load_rom")) {
                if (file.isDirectory() || filePath.endsWith(".zip")) {
                    activity.populateFileGrid(file.getAbsolutePath());
                } else if (file.getAbsolutePath().equals(activity.defaultFilePath)) {
                    activity.setDefaultPath();
                } else {
                    if (!activity.romClicked) {
                        // prevent double clicks which mess up SDL
                        activity.romClicked = true;

                        // cast File to EmuFile and load ROM data from it
                        EmuFile emuFile = (EmuFile) file;
                        RomData romData = new RomData(emuFile);

                        // check ROM is validly initialised
                        if (!romData.isReady()) {
                            activity.showMessage("Can't open ROM file, error encountered");
                            return;
                        }
                        FileBrowserActivity.transferData = romData;

                        if (OptionStore.game_genie) {
                            // get CRC32 checksum
                            CRC32 crcGen = new CRC32();
                            crcGen.update(romData.getRomData());
                            FileBrowserActivity.transferChecksum = Long.toHexString(crcGen.getValue());
                            Intent codesIntent = new Intent(activity, CodesActivity.class);
                            activity.startActivity(codesIntent);
                        }
                        else {
                            Intent sdlIntent = new Intent(activity, SDLActivity.class);
                            activity.startActivity(sdlIntent);
                        }

                        activity.finish();
                    }
                }
            }
            else if (activity.actionType.equals("import_states")) {
                if (file.isDirectory()) {
                    activity.populateFileGrid(file.getAbsolutePath());
                }
                else {
                    // create dialogue
                    activity.importPath = file.getAbsolutePath();
                    AlertDialog importMenu = new AlertDialog.Builder(activity).create();
                    importMenu.setTitle("Import Prompt");
                    importMenu.setMessage("Are you sure you want to import from " + file.getName() + "?");
                    ImportListener il = new ImportListener(activity);
                    importMenu.setButton(DialogInterface.BUTTON_POSITIVE, "YES", il);
                    importMenu.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", il);
                    importMenu.show();
                }
            }
            else if (activity.actionType.equals("export_states")) {
                if (file.isDirectory()) {
                    activity.populateFileGrid(file.getAbsolutePath());
                }
                else {
                    // check for permissions
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) { // not granted
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    activity.FILEBROWSER_WRITE_STORAGE_REQUEST);
                        }
                        else {
                            activity.exportMethod();
                        }
                    }
                    else {
                        activity.exportMethod();
                    }
                }
            }
        }
    }
    public void updateList(Object[] filesAndFolders){
        this.filesAndFolders = filesAndFolders;
        notifyDataSetChanged();
    }
}
