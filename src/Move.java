import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class Move implements Initializable
{
    static final int IN = 19;
    static final int N = IN + 1;
    static final int LEN = 3;//Length of L Move

    private int openedStates;
    private ArrayList<State> states;//Path to root

    private boolean[][] grid;//For Barriers

    private boolean isSetBarrier;//Switch set barrier mode
    private boolean isStart;
    private boolean flip;//For flip between empty white and black

    @FXML
    TilePane squares;

    @FXML
    Label bottomTxt;

    private ImageView img;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < N; j++)
            {
                if (flip)
                    img = new ImageView("empty_white.png");
                else
                    img = new ImageView("empty_black.png");

                flip = !flip;

                squares.getChildren().add(img);
            }
            flip = !flip;
        }

        grid = new boolean[N][N];

        openedStates = 0;
        states = new ArrayList<>();

        openedStates++;
        states.add(new State(0, 0, 0, IN + IN));

        squares.setOnMouseClicked(event ->
        {
            if (isSetBarrier)
                setImgs();
        });

    }

    @FXML
    void setBtnClicked(ActionEvent event)
    {
        if (isStart)
            return;

        isSetBarrier = true;

        setImgs();
    }

    @FXML
    void startBtnClicked(ActionEvent event)
    {
        if (isStart)
            return;

        isStart = true;

        traverse(new State(0, 0, 0, IN + IN)/*root node*/,
                            Integer.MAX_VALUE/*Infinity*/);

        drawKnight();

        //print();
    }

    @FXML
    void pathBtnClicked(ActionEvent event)
    {
        if (isStart)
            drawPath();
    }

    @FXML
    void resetBtnClicked(ActionEvent event)
    {
        squares.getChildren().clear();

        isSetBarrier = false;
        isStart = false;

        ResourceBundle r = new ResourceBundle()
        {
            @Override
            protected Object handleGetObject(String key)
            {
                return null;
            }

            @Override
            public Enumeration<String> getKeys()
            {
                return null;
            }
        };

        URL u;
        try {
            u = new URL("http://test.com");
            initialize(u, r);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void drawKnight()
    {
        for (State knight: states)
        {
            img = new ImageView("knight.png");

            int position = knight.x * N + knight.y;

            squares.getChildren().set(position, img);
        }
    }

    private void drawPath()
    {
        for (int i = 0; i < states.size() - 1; i++)
            fillPath(states.get(i), states.get(i+1));
    }

    private void fillPath(State s1, State s2)
    {
        //Direction move of knight
        final int[] X = {2, 1, -1, -2, -2, -1, 1, 2};
        final int[] Y = {1, 2, 2,  1,  -1, -2,-2, -1};

        int j;
        for (j = 0; j < 8; j++)
        {
            int x = s1.x + X[j];
            int y = s1.y + Y[j];

            if (x == s2.x && y == s2.y)
                break;
        }

        if (s1.x + 1 == s2.x || s1.x - 1 == s2.x)
            for (int k = Y[j] - 1; k < (Y[j]+1); k++)
            {
                int p = s1.x;
                int q = s1.y + k;

                int position = p * N + q;

                img = new ImageView("path.png");

                squares.getChildren().set(position, img);
            }
        else
            for (int k = X[j] - 1; k < (X[j]+1); k++)
            {
                int p = s1.x + k;
                int q = s1.y;

                int position = p * N + q;

                img = new ImageView("path.png");

                squares.getChildren().set(position, img);
            }

    }

    private void setImgs()
    {
        if (isStart)
            return;

        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < N; j++)
            {
                if (grid[i][j])
                    img = new ImageView("barrier.png");
                else if (flip)
                    img = new ImageView("empty_white.png");
                else
                    img = new ImageView("empty_black.png");

                flip = !flip;

                int pos = i * N + j;

                squares.getChildren().set(pos, img);

                final int x = i, y = j;

                img.setOnMouseClicked(event ->
                {
                    if (isStart)
                        return;

                    /*if (grid[x][y])
                    {
                        grid[x][y] = false;
                        if (flip)
                            img = new ImageView("empty_white.png");
                        else
                            img = new ImageView("empty_black.png");
                    }
                    else
                    {*/
                    grid[x][y] = true;
                    img = new ImageView("barrier.png");
                    //}
                    squares.getChildren().set(pos, img);
                });
            }
            flip = !flip;
        }
    }

    private ArrayList<State> findMoves(int x, int y)
    {
        //Direction move of knight
        int[] X = {2, 1, -1, -2, -2, -1, 1, 2};
        int[] Y = {1, 2, 2,  1,  -1, -2,-2, -1};

        ArrayList<State> child = new ArrayList<>();

        //states.size() = depth of tree
        int g1 = states.size() * LEN;
        for (int i = 0; i < 8; i++)
        {
            int p = x + X[i];
            int q = y + Y[i];

            if (p >= 0 && q >= 0 && p <= IN && q <= IN && !grid[p][q])
            {
                State temp = new State();
                temp.x = p;
                temp.y = q;
                temp.h = (IN - p) + (IN - q);
                temp.g = g1;
                child.add(temp);
            }
        }

        return child;
    }

    private int getMin(ArrayList<State> child)
    {
        int result = 0;
        int minF = child.get(0).g + child.get(0).h;

        for (int i = 1; i < child.size(); i++)
        {
            int f = child.get(i).g + child.get(i).h;
            if (f < minF)
            {
                minF = f;
                result = i;
            }
        }
        return result;
    }

    //Compute f limit between parent and siblings
    private int getFlimit(ArrayList<State> s,int parentLimit , int openIndex)
    {
        int minF = parentLimit;

        for (int i = 0; i < s.size(); i++)
        {
            if (i == openIndex)
                continue;

            int f = s.get(i).g + s.get(i).h;
            if (f < minF)
                minF = f;
        }

        if (minF == Integer.MAX_VALUE)
            return minF;

        return minF;
    }

    //Return true if f() all children > current f limit
    private boolean hasFail(ArrayList<State> child,  int flimit)
    {
        int counter = 0;
        for (State temp: child)
            if ((temp.g + temp.h) > flimit)
                counter++;

        return (counter == child.size());
    }

    boolean traverse(State parent, int flimit)
    {

        if (parent.x == IN && parent.y == IN)
        {
            bottomTxt.setStyle("-fx-border-color: lime;" +
                               "-fx-font-weight: bold;");

            bottomTxt.setText("Success by " + states.size() +
                              " Move and Open " + openedStates + " State");

            return true;
        }

        ArrayList<State> child = findMoves(parent.x, parent.y);

        if (child.size() == 0)
            return false;

        boolean isDone = false;
        while (!isDone)
        {
            int minChild = getMin(child);

            State open = child.get(minChild);

            if (hasFail(child, flimit))
            {
                states.remove(parent);
                parent.g = open.g;
                parent.h = open.h;

                return false;
            }

            int fl = getFlimit(child, flimit, child.indexOf(open));

            openedStates++;
            states.add(open);

            isDone = traverse(child.get(minChild), fl);
        }

        return true;
    }

    void print()
    {
        for (State state: states)
            System.out.println(state.x +":" + state.y + ":" + state.g + ":" + state.h);
    }
}
