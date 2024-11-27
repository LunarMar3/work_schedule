package pers.ember.myapplication.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import pers.ember.myapplication.Entity.ProgressNode;
import pers.ember.myapplication.R;
import pers.ember.myapplication.View.ProgressGraphView;

public class ProgressFragment extends Fragment {
    private ProgressGraphView progressGraphView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_progress, container, false);
        progressGraphView = view.findViewById(R.id.progressGraphView);
        Gson gson = new Gson();
        String jsonData ="[\n" +
                "  {\n" +
                "    \"id\": \"1\",\n" +
                "    \"name\": \"Minecraft\",\n" +
                "    \"description\": \"游戏的核心与故事\",\n" +
                "    \"icon\": \"icon1.png\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"x\": 100,\n" +
                "    \"y\": 500,\n" +
                "    \"next\": [\"2\"]\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"2\",\n" +
                "    \"name\": \"石器时代\",\n" +
                "    \"description\": \"用你的新镐挖掘石头\",\n" +
                "    \"icon\": \"icon2.png\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"x\": 400,\n" +
                "    \"y\": 500,\n" +
                "    \"before\": \"1\",\n" +
                "    \"next\": [\"3\"]\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"3\",\n" +
                "    \"name\": \"获得升级\",\n" +
                "    \"description\": \"制作一把更好的镐\",\n" +
                "    \"icon\": \"icon3.png\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"x\": 700,\n" +
                "    \"y\": 500,\n" +
                "    \"before\": \"2\",\n" +
                "    \"next\": [\"4\"]\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"4\",\n" +
                "    \"name\": \"来硬的\",\n" +
                "    \"description\": \"冶炼出一块铁锭\",\n" +
                "    \"icon\": \"icon4.png\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"before\": \"3\",\n" +
                "    \"x\": 1000,\n" +
                "    \"y\": 500,\n" +
                "    \"next\": [\"5\",\"6\",\"7\"]\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"5\",\n" +
                "    \"name\": \"这不是铁镐么\",\n" +
                "    \"description\": \"升级你的镐\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"before\": \"4\",\n" +
                "    \"x\": 1300,\n" +
                "    \"y\": 500,\n" +
                "    \"icon\": \"icon5.png\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"6\",\n" +
                "    \"name\": \"热腾腾的\",\n" +
                "    \"description\": \"用铁桶装点熔岩\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"before\": \"4\",\n" +
                "    \"x\": 1300,\n" +
                "    \"y\": 800,\n" +
                "    \"icon\": \"icon6.png\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"7\",\n" +
                "    \"name\": \"整装上阵\",\n" +
                "    \"description\": \"用铁盔甲来保护你自己\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"before\": \"4\",\n" +
                "    \"x\": 1300,\n" +
                "    \"y\": 1100,\n" +
                "    \"icon\": \"icon6.png\"\n" +
                "  }\n" +
                "] ";
        List<ProgressNode> nodes = Arrays.asList(gson.fromJson(jsonData, ProgressNode[].class));
        progressGraphView.setNodes(nodes);

        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        progressGraphView = null;
    }
}
