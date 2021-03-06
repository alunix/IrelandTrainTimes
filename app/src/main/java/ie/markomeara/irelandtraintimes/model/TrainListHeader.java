package ie.markomeara.irelandtraintimes.model;

import ie.markomeara.irelandtraintimes.adapter.TrainsDueRecyclerViewAdapter;

public class TrainListHeader implements TrainListItem {

    private String headingText;

    public TrainListHeader(String direction){
        this.headingText = direction;
    }

    @Override
    public int getViewType() {
        return TrainsDueRecyclerViewAdapter.RowType.HEADER.ordinal();
    }

    public void setHeadingText(String text){
        this.headingText = text;
    }

    public String getHeadingText(){
        return this.headingText;
    }
}
